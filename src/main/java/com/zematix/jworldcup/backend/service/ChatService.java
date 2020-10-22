package com.zematix.jworldcup.backend.service;

import static com.google.common.base.Preconditions.checkArgument;
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
	 * Returns a list of {@link UserGroup} instances which belongs to the 
	 * given {@link Event#eventId} and {@link User#userId}.
	 * The latter means that userGroup contains the given user as a member of it.
	 * If the given {@code isEverybodyIncluded} parameter is {@code true}, a virtual 
	 * Everybody userGroup is also added to the end of the result list.
	 * 
	 * @param eventId - filter
	 * @param userId - filter
	 * @return list of userGroups which belongs to the given eventId and userId
	 */
	@Transactional(readOnly = true)
	public List<Chat> retrieveChats(UserGroup userGroup) throws ServiceException {
		checkArgument(userGroup != null, "Argument \"userGroup\" cannot be null.");
		
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
		checkArgument(userGroup != null, "Argument \"userGroup\" cannot be null.");
		checkArgument(userId != null, "Argument \"userId\" cannot be null.");
		
		List<ParametrizedMessage> errMsgs = new ArrayList<>();

		if (Strings.isNullOrEmpty(message)) {
			errMsgs.add(ParametrizedMessage.create("MISSING_MESSAGE"));
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
		checkArgument(eventId != null, "Argument \"eventId\" cannot be null.");
		checkArgument(userId != null, "Argument \"userId\" cannot be null.");
		
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
