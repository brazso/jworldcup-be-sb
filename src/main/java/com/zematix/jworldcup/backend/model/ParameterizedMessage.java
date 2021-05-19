package com.zematix.jworldcup.backend.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Locale;

import org.springframework.context.MessageSource;

import com.google.common.base.Joiner;
import com.zematix.jworldcup.backend.emun.ParameterizedMessageType;

/**
 * Structure containing parameterized messages.
  */
public class ParameterizedMessage {

	/**
	 * msgCode identifies the message object
	 */
	private String msgCode = null;
	
	/**
	 * MsgType contains the type of the message object
	 */
	private ParameterizedMessageType msgType = null;
	
	/**
	 * This array contains the parameter values of the message object.
	 */
	private Object[] msgParams = new Object[]{};

	private ParameterizedMessage(String msgCode) {
		this(msgCode, ParameterizedMessageType.ERROR, new Object[]{});
	}

	private ParameterizedMessage(String msgCode, ParameterizedMessageType msgType) {
		this(msgCode, msgType, new Object[]{});
	}

	private ParameterizedMessage(String msgCode, Object... msgParameters) {
		this(msgCode, ParameterizedMessageType.ERROR, msgParameters);
	}
	
	private ParameterizedMessage(String msgCode, ParameterizedMessageType msgType, Object... msgParameters) {
		checkNotNull(msgCode);
		this.msgCode = msgCode;
		
		if (msgType == null) {
			msgType = ParameterizedMessageType.ERROR;
		}
		this.setMsgType(msgType);
		
		if (msgParameters == null) {
			msgParameters = new Object[]{};
		}
		this.msgParams = msgParameters;
	}
	
	public static ParameterizedMessage create(String msgCode) {
		return new ParameterizedMessage(msgCode);
	}

	public static ParameterizedMessage create(String msgCode, ParameterizedMessageType msgType) {
		return new ParameterizedMessage(msgCode, msgType);
	}

	public static ParameterizedMessage create(String msgCode, Object... msgParameters) {
		return new ParameterizedMessage(msgCode, msgParameters);
	}

	public static ParameterizedMessage create(String msgCode, ParameterizedMessageType msgType, Object... msgParameters) {
		return new ParameterizedMessage(msgCode, msgType, msgParameters);
	}

	public String getMsgCode() {
		return msgCode;
	}

	public void setMsgCode(String msgCode) {
		this.msgCode = msgCode;
	}

	public ParameterizedMessageType getMsgType() {
		return msgType;
	}

	public void setMsgType(ParameterizedMessageType msgType) {
		this.msgType = msgType;
	}

	public Object[] getMsgParameters() {
		return msgParams;
	}

	public void setMsgParams(Object[] msgParams) {
		this.msgParams = msgParams;
	}

	public String buildMessage(MessageSource msgs) {
		return buildMessage(msgs, null);
	}

	public String buildMessage(MessageSource msgs, Locale locale) {
		checkNotNull(msgs);
		return msgs.getMessage(this.getMsgCode(), this.getMsgParameters(), locale == null ? Locale.getDefault() : locale);
	}
	
	/**
	 * Helper for trace purpose.
	 */
	@Override
	public String toString() {
		return this.msgType + ": " + this.msgCode + "[" + Joiner.on(",").join(this.msgParams) + "]";
	}
}
