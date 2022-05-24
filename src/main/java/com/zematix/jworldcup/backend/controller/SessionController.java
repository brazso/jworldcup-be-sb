package com.zematix.jworldcup.backend.controller;

import java.util.Map;

import javax.inject.Inject;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zematix.jworldcup.backend.dto.CommonResponse;
import com.zematix.jworldcup.backend.dto.GenericResponse;
import com.zematix.jworldcup.backend.dto.SessionDataDto;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.mapper.SessionDataMapper;
import com.zematix.jworldcup.backend.service.ServiceBase;
import com.zematix.jworldcup.backend.service.SessionService;

import io.swagger.v3.oas.annotations.Operation;

/**
 * WS Rest wrapper class of {@link SessionService}.
 * Only the necessary public methods of its associated service class are in play.
 */
@RestController
@RequestMapping("session")
public class SessionController extends ServiceBase implements ResponseEntityHelper {

//	@Inject
//	private Logger logger;

	@Inject
	private SessionService sessionService;

	@Inject
	private SessionDataMapper sessionDataMapper;
	
	@Autowired
    private SimpMessagingTemplate template;
	
//	@Autowired
//	private RabbitTemplate rabbitTemplate; // loaded but send invokes "SimpleMessageConverter only supports String, byte[] and Serializable payloads"

//	@Autowired
//	private RabbitAdmin rabbitAdmin; // cannot be loaded

//    @Autowired
//    private AmqpTemplate amqpTemplate; // loaded but send invokes "SimpleMessageConverter only supports String, byte[] and Serializable payloads"
    
    @Autowired
    private AmqpAdmin amqpAdmin;

	/**
	 * Refreshes session data storing locale, user and event.
	 * 
	 * @param sessionDataDto
	 * @return refreshed session data
	 * @throws ServiceException if the session data cannot be refreshed
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Refresh session info", description = "Refresh session info")
	@PutMapping(value = "/refresh-session-data")
	public ResponseEntity<GenericResponse<SessionDataDto>> refreshSessionData(
			@RequestBody SessionDataDto sessionDataDto) throws ServiceException {
		var sessionData = sessionService.refreshSessionData(sessionDataMapper.dtoToEntity(sessionDataDto));
		notifySessionData(sessionDataMapper.entityToDto(sessionData)); // test
		return buildResponseEntityWithOK(new GenericResponse<>(sessionDataMapper.entityToDto(sessionData)));
	}

	/**
	 * Notifies session data storing locale, user and event.
	 * Same as {@link refreshSessionData} method but this works via websocket.
	 * 
	 * @param sessionDataDto
	 * @throws ServiceException if the session data cannot be notified
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Notify session info", description = "Notify session info")
	@PutMapping(value = "/notify-session-data")
	public ResponseEntity<CommonResponse> notifySessionData(
			@RequestBody SessionDataDto sessionDataDto) throws ServiceException {
		var sessionData = sessionService.refreshSessionData(sessionDataMapper.dtoToEntity(sessionDataDto));
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (sessionData.getId() != null) {
//			Map<String, Object> headers = Map.of("durable", "true", "auto-delete", "true", "x-expires", 20000, "x-queue-type", "classic");
//			Map<String, Object> headers = Map.of("durable", "true", "auto-delete", "false"); // wrong
//			Map<String, Object> headers = Map.of("x-expires", 20000);
			Map<String, Object> headers = Map.of("durable", "false", "exclusive", "false", "auto-delete", "true");

//			logger.info("create queue");
//			Properties properties = amqpAdmin.getQueueProperties("session"+sessionData.getId());
//			logger.info("properties1: "+properties); // null
//			amqpAdmin.declareQueue(new Queue("session"+sessionData.getId(), /*durable*/ false, /*exclusive*/ false, /*autoDelete*/ true/*, headers*/));
//			properties = amqpAdmin.getQueueProperties("session"+sessionData.getId());
//			logger.info("properties2: "+properties); // {QUEUE_NAME=session2c4130db-efb7-4a54-91a3-d681d68dafad, QUEUE_MESSAGE_COUNT=0, QUEUE_CONSUMER_COUNT=0}

			// creates the queue automatically unless it exits 
			template.convertAndSend("/queue/session"+sessionData.getId(), sessionDataMapper.entityToDto(sessionData), headers);
//			template.convertAndSendToUser(sessionData.getId(), "/queue/session", sessionDataMapper.entityToDto(sessionData), headers); // => template.convertAndSend("/user/"+sessionData.getId()+"/queue/session") but later not translated more 
			
//			org.springframework.messaging.simp.user.UserDestinationMessageHandler a;
//			org.springframework.messaging.simp.stomp.StompBrokerRelayMessageHandler b;
			
//			rabbitTemplate.convertAndSend("/queue/session"+sessionData.getId(), sessionDataMapper.entityToDto(sessionData)); // error: "SimpleMessageConverter only supports String, byte[] and Serializable payloads"
//			amqpTemplate.convertAndSend("/queue/session"+sessionData.getId(), sessionDataMapper.entityToDto(sessionData)); // error: "SimpleMessageConverter only supports String, byte[] and Serializable payloads"
		}
        return buildResponseEntityWithOK(new CommonResponse());
	}

	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Delete queue", description = "Delete queue")
	@DeleteMapping(value = "/delete-queue")
	public ResponseEntity<CommonResponse> deleteQueue(
			@RequestParam String destination) throws ServiceException {
//		boolean isDone = rabbitAdmin.deleteQueue(destination);
		boolean isDone = amqpAdmin.deleteQueue(destination); // works
		return buildResponseEntityWithOK(new CommonResponse(isDone));
	}
}
