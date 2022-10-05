package com.zematix.jworldcup.backend.service;

import java.util.Map;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.zematix.jworldcup.backend.entity.Chat;
import com.zematix.jworldcup.backend.entity.UserGroup;
import com.zematix.jworldcup.backend.mapper.ChatMapper;
import com.zematix.jworldcup.backend.mapper.SessionDataMapper;
import com.zematix.jworldcup.backend.model.SessionData;

@Service
public class MessageQueueService extends ServiceBase{

	@Autowired
    private SimpMessagingTemplate template;

	@Inject
	private SessionDataMapper sessionDataMapper;

	@Inject
	private ChatMapper chatMapper;

	/**
	 * Sends the given session data to a session queue.
	 * @param sessionData
	 */
	public void sendSession(SessionData sessionData) {
		final Map<String, Object> headers = Map.of("durable", "false", "exclusive", "false", "auto-delete", "true");

//		logger.info("create queue");
//		Properties properties = amqpAdmin.getQueueProperties("session"+sessionData.getId());
//		logger.info("properties1: "+properties); // null
//		amqpAdmin.declareQueue(new Queue("session"+sessionData.getId(), /*durable*/ false, /*exclusive*/ false, /*autoDelete*/ true/*, headers*/));
//		properties = amqpAdmin.getQueueProperties("session"+sessionData.getId());
//		logger.info("properties2: "+properties); // {QUEUE_NAME=session2c4130db-efb7-4a54-91a3-d681d68dafad, QUEUE_MESSAGE_COUNT=0, QUEUE_CONSUMER_COUNT=0}

		// creates the queue automatically unless it exits 
		template.convertAndSend("/queue/session#"+sessionData.getId(), sessionDataMapper.entityToDto(sessionData), headers);
	}
	
	/**
	 * Sends the given chat object to an user-group topic.
	 * @param chat
	 */
	public void sendChat(Chat chat) {
		final Map<String, Object> headers = Map.of("durable", "false", "exclusive", "false", "auto-delete", "false");

		Long userGroupId = chat.getUserGroup() != null ? chat.getUserGroup().getUserGroupId() : UserGroup.EVERYBODY_USER_GROUP_ID;
		
		// creates the queue automatically unless it exits 
		template.convertAndSend("/topic/chat#"+userGroupId, chatMapper.entityToDto(chat), headers);
	}
	
	/**
	 * Sends the given private chat object to an user queue.
	 * @param chat
	 */
	public void sendPrivateChat(Chat chat) {
		final Map<String, Object> headers = Map.of("durable", "false", "exclusive", "false", "auto-delete", "true");

		// creates the queue automatically unless it exits 
		template.convertAndSend("/queue/privatechat#"+chat.getTargetUser().getUserId(), chatMapper.entityToDto(chat), headers);
	}
}
