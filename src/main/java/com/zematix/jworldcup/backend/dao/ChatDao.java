package com.zematix.jworldcup.backend.dao;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;
import com.zematix.jworldcup.backend.entity.Chat;
import com.zematix.jworldcup.backend.entity.QChat;
import com.zematix.jworldcup.backend.entity.UserGroup;
import com.zematix.jworldcup.backend.exception.ServiceException;

/**
 * Database operations around {@link UserGroup} entities.
 */
@Component
@Transactional
public class ChatDao extends DaoBase {

	public static final long MAX_USERGROUP_CHAT_MESSAGES = 50;

//	@Inject
//	private CommonDao commonDao;
	
	/**
	 * Returns a list of all {@link Chat} entities from database.
	 * 
	 * @return list of all {@link Chat} entities
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Chat> findAllChats() {
		TypedQuery<Chat> query = getEntityManager().createNamedQuery("Chat.findAll", Chat.class);
		return query.getResultList();
	}
	
	/**
	 * Returns a list of chat objects belong to the given {@code userGroupId}. If the latter has
	 * {@link UserGroup#EVERYBODY_USER_GROUP_ID} ID then the result is calculated from
	 * the given {@code eventId}.
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Chat> retrieveChats(Long eventId, Long userGroupId) throws ServiceException {
		List<Chat> chats;
		checkNotNull(userGroupId);
		checkArgument(!(userGroupId == UserGroup.EVERYBODY_USER_GROUP_ID && eventId == null), "Argument \"eventId\" cannot be null if argument \"userGroupId\" is virtual everybody.");
		
		if (userGroupId == UserGroup.EVERYBODY_USER_GROUP_ID) {
			QChat qChat = QChat.chat;
			JPAQuery<Chat> query = new JPAQuery<>(getEntityManager());
			chats = query.from(qChat)
			  .where(qChat.userGroup.isNull()
					  .and(qChat.event.eventId.eq(eventId)))
			  .orderBy(qChat.chatId.asc())
			  .limit(MAX_USERGROUP_CHAT_MESSAGES)
			  .fetch();
		}
		else {
			QChat qChat = QChat.chat;
			JPAQuery<Chat> query = new JPAQuery<>(getEntityManager());
			chats = query.from(qChat)
			  .where(qChat.userGroup.userGroupId.eq(userGroupId))
			  .orderBy(qChat.chatId.asc())
			  .limit(MAX_USERGROUP_CHAT_MESSAGES)
			  .fetch();
		}
		
		return chats;
	}

	/**
	 * Enforces {@link ChatDao#MAX_USERGROUP_CHAT_MESSAGES} limit on chat rows in database.
	 * The given (virtual) userGroup cannot contain more rows than the limit. The surplus
	 * rows are being deleted.
	 */
	public boolean truncateChats(Long eventId, Long userGroupId) throws ServiceException {
		List<Chat> chats;
		checkNotNull(userGroupId);
		checkArgument(!(userGroupId == UserGroup.EVERYBODY_USER_GROUP_ID && eventId == null), "Argument \"eventId\" cannot be null if argument \"userGroupId\" is virtual everybody.");

		QChat qChat = QChat.chat;
		JPAQuery<Chat> query = new JPAQuery<>(getEntityManager());
		if (userGroupId == UserGroup.EVERYBODY_USER_GROUP_ID) {
			chats = query.from(qChat)
			  .where(qChat.userGroup.isNull()
					  .and(qChat.event.eventId.eq(eventId)))
			  .orderBy(qChat.chatId.asc())
			  .limit(MAX_USERGROUP_CHAT_MESSAGES+1)
			  .fetch();
		}
		else {
			chats = query.from(qChat)
			  .where(qChat.userGroup.userGroupId.eq(userGroupId))
			  .orderBy(qChat.chatId.asc())
			  .limit(MAX_USERGROUP_CHAT_MESSAGES+1)
			  .fetch();
		}
		
		if (chats.size() <= MAX_USERGROUP_CHAT_MESSAGES) {
			return false;
		}
		
		Chat surplusChat = chats.get(0);
		
		JPADeleteClause clause = new JPADeleteClause(getEntityManager(), qChat);
		if (userGroupId == UserGroup.EVERYBODY_USER_GROUP_ID) {
			clause.where(qChat.userGroup.isNull()
					  .and(qChat.event.eventId.eq(eventId))
					  .and(qChat.chatId.loe(surplusChat.getChatId())))
			  .execute();
		}
		else {
			clause.where(qChat.userGroup.userGroupId.eq(userGroupId)
					  .and(qChat.chatId.loe(surplusChat.getChatId())))
			  .execute();
		}
		
		return true;
	}

	/**
	 * Return latest chat record which belongs to the given {@code eventId} and {@code user}.
	 * via some {@link UserGroup}. Unless it is found it returns {@code null}.
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Chat retrieveLatestChat(Long eventId, Long userId) throws ServiceException {
		Chat chat = null;
		checkNotNull(eventId);
		checkNotNull(userId);

		QChat qChat = QChat.chat;
		JPAQuery<Chat> query = new JPAQuery<>(getEntityManager());
		chat = query.from(qChat)
		  .where(qChat.event.eventId.eq(eventId)
				  .and(qChat.userGroup.isNull().or(qChat.userGroup.users.any().userId.eq(userId))))
		  .orderBy(qChat.modificationTime.desc())
		  .fetchFirst();
		
		return chat;
	}

}
