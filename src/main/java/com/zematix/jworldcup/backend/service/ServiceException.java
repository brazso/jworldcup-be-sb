package com.zematix.jworldcup.backend.service;

import java.util.List;

/**
 * Exception used by service classes.
 * It contains a list of messages, they are mostly derived from validation errors.
 */
public class ServiceException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * list of {@link ParametrizedMessage} elements as messages
	 */
	private final transient List<ParametrizedMessage> messages;
	
	public ServiceException(List<ParametrizedMessage> messages) {
		super();
		this.messages = messages;
	}
 
	public List<ParametrizedMessage> getMessages() {
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
	public ParametrizedMessageType getOverallType() {
		ParametrizedMessageType msgType = null;
		
		for (ParametrizedMessage msg : messages) {
			switch (msg.getMsgType()) {
				case ERROR:
					msgType = ParametrizedMessageType.ERROR;
					return msgType;
				case WARNING:
					msgType = ParametrizedMessageType.WARNING;
					break;
				case INFO:
					if (msgType != ParametrizedMessageType.WARNING) {
						msgType = ParametrizedMessageType.INFO;
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
		for (ParametrizedMessage msg : messages) {
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
		List<ParametrizedMessage> pMsgs = this.getMessages();
		for (ParametrizedMessage pMsg : pMsgs) {
			if (!pMsgs.get(0).equals(pMsg)) { // not first element?
				sb.append(", ");
			}
			sb.append(pMsg.toString());
		}
		return sb.toString();
	}
}