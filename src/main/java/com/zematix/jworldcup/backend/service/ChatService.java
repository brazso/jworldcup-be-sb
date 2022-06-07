package com.zematix.jworldcup.backend.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

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
import com.zematix.jworldcup.backend.entity.User;
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
		chats.stream().forEach(e -> {e.getUser()/*.getLoginName()*/; e.getEvent(); });
		
		return chats;
	}
	
	/**
	 * Persists new chat entity.
	 * 
	 * @param userGroup
	 * @param userId
	 * @param message
	 * @return {@code true} if persist was successful, {@code false} otherwise
	 */
	@Transactional
	public void sendChatMessage(UserGroup userGroup, Long userId, String message) throws ServiceException {
		checkNotNull(userGroup);
		checkNotNull(userId);
		
		List<ParameterizedMessage> errMsgs = new ArrayList<>();

		if (Strings.isNullOrEmpty(message)) {
			errMsgs.add(ParameterizedMessage.create("MISSING_MESSAGE"));
			throw new ServiceException(errMsgs);
		}
		
		Event event = userGroup.getEvent();
		checkState(event != null, "No loaded \"Event\" entity belongs to \"userGroup\" argument.");
		User user = commonDao.findEntityById(User.class, userId);
		checkState(user != null, "No \"User\" entity belongs to \"userId\"=%d in database.", userId);

		LocalDateTime actualDateTime = applicationService.getActualDateTime();
		
		Chat chat = new Chat();
		chat.setEvent(event);
		chat.setUser(user);
		if (userGroup.getUserGroupId().equals(UserGroup.EVERYBODY_USER_GROUP_ID)) {
			chat.setUserGroup(null);
		}
		else {
			chat.setUserGroup(userGroup);
		}
		chat.setModificationTime(actualDateTime);
		chat.setMessage(message);
		commonDao.persistEntity(chat);
		commonDao.flushEntityManager();
		
		chatDao.truncateChats(event.getEventId(), userGroup.getUserGroupId());
		applicationService.refreshChatsByUserGroupCache(userGroup);
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
}
