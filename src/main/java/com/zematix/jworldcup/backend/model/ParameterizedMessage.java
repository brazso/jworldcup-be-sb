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

	private ParameterizedMessage(String msgCode, Object... msgParams) {
		this(msgCode, ParameterizedMessageType.ERROR, msgParams);
	}
	
	private ParameterizedMessage(String msgCode, ParameterizedMessageType msgType, Object... msgParams) {
		checkNotNull(msgCode);
		this.msgCode = msgCode;
		
		if (msgType == null) {
			msgType = ParameterizedMessageType.ERROR;
		}
		this.setMsgType(msgType);
		
		if (msgParams == null) {
			msgParams = new Object[]{};
		}
		this.msgParams = msgParams;
	}
	
	public static ParameterizedMessage create(String msgCode) {
		return new ParameterizedMessage(msgCode);
	}

	public static ParameterizedMessage create(String msgCode, ParameterizedMessageType msgType) {
		return new ParameterizedMessage(msgCode, msgType);
	}

	public static ParameterizedMessage create(String msgCode, Object... msgParams) {
		return new ParameterizedMessage(msgCode, msgParams);
	}

	public static ParameterizedMessage create(String msgCode, ParameterizedMessageType msgType, Object... msgParams) {
		return new ParameterizedMessage(msgCode, msgType, msgParams);
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

	public Object[] getMsgParams() {
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
		return msgs.getMessage(this.getMsgCode(), this.getMsgParams(), locale == null ? Locale.getDefault() : locale);
	}
	
	/**
	 * Helper for trace purpose.
	 */
	@Override
	public String toString() {
		return this.msgType + ": " + this.msgCode + "[" + Joiner.on(",").join(this.msgParams) + "]";
	}
}
