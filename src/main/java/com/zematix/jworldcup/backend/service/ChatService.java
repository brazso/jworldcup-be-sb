package com.zematix.jworldcup.backend.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.zematix.jworldcup.backend.dao.ChatDao;
import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.entity.Chat;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.UserGroup;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.model.ParameterizedMessage;

/**
 * Operations around {@link Chat} elements. 
 * Usually it injects at least its DAO class belongs to the same entities. 
 * It may also inject other services and DAO classes.
 */
@Service
@Transactional
public class ChatService extends ServiceBase {
	
	@Inject
	private ChatDao chatDao;
	
	@Inject 
	private UserGroupService userGroupService;
	
	@Inject
	private ApplicationService applicationService;
	
	@Inject
	private MessageQueueService queueService;
	
	@Inject
	private CommonDao commonDao;

	/**
	 * Facade to {@link ChatDao#findAllChats()} method.
	 * 
	 * @return list of all {@link Chat} entities
	 */
	@Transactional(readOnly = true)
	public List<Chat> findAllChats() {
		List<Chat> chats = chatDao.findAllChats();
//		chats.forEach(e -> {
//			e.getUser().getRoles().size(); // lazy-load
//		});
		return chats;
	}

	/**
	 * Returns a list of {@link Chat} instances which belongs to the 
	 * given {@link UserGroup} instance. From the latter object {@link Usergroup#eventId} 
	 * and {@link UserGroup#userGroupId} are used.
	 * 
	 * @param userGroup - filter
	 * @return list of chats which belongs to the eventId and userGroupId of the given userGroup
	 */
	@Transactional(readOnly = true)
	public List<Chat> retrieveChats(UserGroup userGroup) throws ServiceException {
		checkNotNull(userGroup);
		
		List<Chat> chats = chatDao.retrieveChats(userGroup.getEvent().getEventId(), userGroup.getUserGroupId());
		
		// load lazy associations
		chats.stream().forEach(e -> {
			e.getUser().getLoginName();
			e.getUser().getRoles().size();
			e.getEvent().getDescription();
			if (e.getUserGroup() != null) {
				e.getUserGroup().getName();
			}
		});
		
		return chats;
	}
	
	/**
	 * Persists new chat entity.
	 * 
	 * @param userGroup
	 * @param userId
	 * @param message
	 * @return sent chat instance
	 */
	@Transactional
	public Chat sendChat(Chat chat) throws ServiceException {
		checkNotNull(chat);
		checkNotNull(chat.getEvent());
		checkNotNull(chat.getUser());
		
		List<ParameterizedMessage> errMsgs = new ArrayList<>();

		if (Strings.isNullOrEmpty(chat.getMessage())) {
			errMsgs.add(ParameterizedMessage.create("MISSING_MESSAGE"));
			throw new ServiceException(errMsgs);
		}

		// private chat is handled distinguished
		if (chat.getUserGroup() == null && chat.getTargetUser() != null) {
			return this.sendPrivateChat(chat);
		}
		
		LocalDateTime actualDateTime = applicationService.getActualDateTime();
		
		UserGroup userGroup = chat.getUserGroup();
		if (userGroup.getUserGroupId().equals(UserGroup.EVERYBODY_USER_GROUP_ID)) {
			chat.setUserGroup(null);
		}
		chat.setModificationTime(actualDateTime);
		commonDao.persistEntity(chat);
		commonDao.flushEntityManager();
		commonDao.detachEntity(chat);
		chat.setUserGroup(userGroup); // restore original value
		
		chatDao.truncateChats(chat.getEvent().getEventId(), userGroup.getUserGroupId());
		applicationService.refreshChatsByUserGroupCache(userGroup);

		queueService.sendChat(chat);
		return chat;
	}
	
	@Transactional(readOnly = true)
	public Chat retrieveLatestChat(Long eventId, Long userId) throws ServiceException {
		checkNotNull(eventId);
		checkNotNull(userId);
		
		Chat chat = chatDao.retrieveLatestChat(eventId, userId);
		commonDao.detachEntity(chat); // later there might be called setUserGroup at virtual user group
		if (chat != null) {
			chat.getUser().getLoginName();
			if (chat.getUserGroup() != null) {
				chat.getUserGroup().getName();
			}
			else {
				chat.setUserGroup(userGroupService.createVirtualEverybodyUserGroup(eventId, userId));
			}
		}

		return chat; 
	}

	/**
	 * Returns a list of private {@link Chat} instances which belongs to the 
	 * given event {@link Event}, source and target {@link User} instances.
	 * 
	 * @param eventId - filter (optional)
	 * @param sourceUserId - filter
	 * @param targeteUserId - filter
	 * @return list of private chats which belongs to the given event, source and target users
	 */
	public List<Chat> retrievePrivateChats(Long eventId, Long sourceUserId, Long targetUserId) throws ServiceException {
		checkNotNull(sourceUserId);
		checkNotNull(targetUserId);

		List<Chat> chats = chatDao.retrievePrivateChats(eventId, sourceUserId, targetUserId);

		// update accessTime - it is null yet - aimed to sourceUserId  
		LocalDateTime actualDateTime = applicationService.getActualDateTime();
		for (Chat chat: chats) {
			if (chat.getAccessTime() == null && chat.getTargetUser().getUserId().equals(sourceUserId)) {
				chat.setAccessTime(actualDateTime);
			}
		}

		// load lazy associations
		chats.stream().forEach(e -> {
			e.getUser().getLoginName();
			e.getUser().getRoles().size();
			e.getEvent().getDescription();
			if (e.getUserGroup() != null) {
				e.getUserGroup().getName();
			}
		});
		
		return chats;
	}

	/**
	 * Persists new private chat entity.
	 * 
	 * @param userGroup
	 * @param userId
	 * @param message
	 * @return sent chat instance
	 */
	private Chat sendPrivateChat(Chat chat) throws ServiceException {
		checkNotNull(chat);
		checkNotNull(chat.getEvent());
		checkNotNull(chat.getUser());
		checkNotNull(chat.getTargetUser());
		
		List<ParameterizedMessage> errMsgs = new ArrayList<>();

		if (Strings.isNullOrEmpty(chat.getMessage())) {
			errMsgs.add(ParameterizedMessage.create("MISSING_MESSAGE"));
			throw new ServiceException(errMsgs);
		}
		
		LocalDateTime actualDateTime = applicationService.getActualDateTime();
		
		chat.setModificationTime(actualDateTime);
		commonDao.persistEntity(chat);
		commonDao.flushEntityManager();
		commonDao.detachEntity(chat);
		
		chatDao.truncatePrivateChats(null, chat.getUser().getUserId(), chat.getTargetUser().getUserId()); // truncates independently on eventId

		queueService.sendPrivateChat(chat);
		return chat;
	}}