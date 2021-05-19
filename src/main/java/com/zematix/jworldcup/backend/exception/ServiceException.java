package com.zematix.jworldcup.backend.exception;

import java.util.List;

import com.zematix.jworldcup.backend.emun.ParameterizedMessageType;
import com.zematix.jworldcup.backend.model.ParameterizedMessage;

/**
 * Exception used by service classes.
 * It contains a list of messages, they are mostly derived from validation errors.
 */
public class ServiceException extends Exception {

	private static final long serialVersionUID = 1L;
	
	/**
	 * list of {@link ParameterizedMessage} elements as messages
	 */
	private final transient List<ParameterizedMessage> messages;
	
	public ServiceException(List<ParameterizedMessage> messages) {
		super();
		this.messages = messages;
	}
 
	public List<ParameterizedMessage> getMessages() {
		return messages;
	}
	
	/**
	 * Returns overall (@link ParametrizedMessageType) value based on 
	 * the following rules: 
	 * <li>if contains at least one ERROR, it returns ERROR</li> 
	 * <li>else if contains at least one WARNING, it returns WARNING</li>
	 * <li>else if contains at least one INFO, it returns INFO</li>
	 * <li>otherwise it returns {@code null}
	 * 
	 * @return overall type of the messages contained by the exception object
	 */
	public ParameterizedMessageType getOverallType() {
		ParameterizedMessageType msgType = null;
		
		for (ParameterizedMessage msg : messages) {
			switch (msg.getMsgType()) {
				case ERROR:
					msgType = ParameterizedMessageType.ERROR;
					return msgType;
				case WARNING:
					msgType = ParameterizedMessageType.WARNING;
					break;
				case INFO:
					if (msgType != ParameterizedMessageType.WARNING) {
						msgType = ParameterizedMessageType.INFO;
					}
					break;
			}
		}
		return msgType;
	}
	
	/**
	 * Checks that given msgCode is found among {@link ServiceException#messages}
	 * 
	 * @param msgCode
	 * @return true if {@link ServiceException#messages} contains message with {@code msgCode}
	 */
	public boolean containsMessage(String msgCode) {
		for (ParameterizedMessage msg : messages) {
			if (msg.getMsgCode().equals(msgCode)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Helper for trace purpose.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		List<ParameterizedMessage> pMsgs = this.getMessages();
		for (ParameterizedMessage pMsg : pMsgs) {
			if (!pMsgs.get(0).equals(pMsg)) { // not first element?
				sb.append(", ");
			}
			sb.append(pMsg.toString());
		}
		return sb.toString();
	}
}