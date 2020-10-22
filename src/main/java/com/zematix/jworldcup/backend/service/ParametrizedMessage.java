package com.zematix.jworldcup.backend.service;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Joiner;

/**
 * Structure containing parametrized messages.
 *
 */
public class ParametrizedMessage {

	/**
	 * msgCode identifies the message object
	 */
	private String msgCode = null;
	
	/**
	 * MsgType contains the type of the message object
	 */
	private ParametrizedMessageType msgType = null;
	
	/**
	 * This array contains the parameter values of the message object.
	 */
	private Object[] msgParams = new Object[]{};

	private ParametrizedMessage(String msgCode) {
		this(msgCode, ParametrizedMessageType.ERROR, new Object[]{});
	}

	private ParametrizedMessage(String msgCode, ParametrizedMessageType msgType) {
		this(msgCode, msgType, new Object[]{});
	}

	private ParametrizedMessage(String msgCode, Object... msgParameters) {
		this(msgCode, ParametrizedMessageType.ERROR, msgParameters);
	}
	
	private ParametrizedMessage(String msgCode, ParametrizedMessageType msgType, Object... msgParameters) {
		checkArgument(msgCode != null );
		this.msgCode = msgCode;
		
		if (msgType == null) {
			msgType = ParametrizedMessageType.ERROR;
		}
		this.setMsgType(msgType);
		
		if (msgParameters == null) {
			msgParameters = new Object[]{};
		}
		this.msgParams = msgParameters;
	}
	
	public static ParametrizedMessage create(String msgCode) {
		return new ParametrizedMessage(msgCode);
	}

	public static ParametrizedMessage create(String msgCode, ParametrizedMessageType msgType) {
		return new ParametrizedMessage(msgCode, msgType);
	}

	public static ParametrizedMessage create(String msgCode, Object... msgParameters) {
		return new ParametrizedMessage(msgCode, msgParameters);
	}

	public static ParametrizedMessage create(String msgCode, ParametrizedMessageType msgType, Object... msgParameters) {
		return new ParametrizedMessage(msgCode, msgType, msgParameters);
	}

	public String getMsgCode() {
		return msgCode;
	}

	public void setMsgCode(String msgCode) {
		this.msgCode = msgCode;
	}

	public ParametrizedMessageType getMsgType() {
		return msgType;
	}

	public void setMsgType(ParametrizedMessageType msgType) {
		this.msgType = msgType;
	}

	public Object[] getMsgParameters() {
		return msgParams;
	}

	public void setMsgParams(Object[] msgParams) {
		this.msgParams = msgParams;
	}
	
	/**
	 * Helper for trace purpose.
	 */
	@Override
	public String toString() {
		return this.msgType + ": " + this.msgCode + "[" + Joiner.on(",").join(this.msgParams) + "]";
	}
}
