package com.zematix.jworldcup.backend.service;

import java.text.MessageFormat;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.model.ParameterizedMessage;

/**
 * Common ancestor of lots of Server classes.
 */
public abstract class ServerBase {

	@Inject
	protected Logger logger;
	
	public ServerBase () {
		if (logger == null) {
			// set logger in non CDI environment
			logger = LoggerFactory.getLogger(getClass());
		}
	}

	/**
	 * Consumes ServiceException instance, logs its all
	 * messages.
	 * 
	 * @param e - ServiceException instance to be consumed
	 */
	protected void consumeServiceException(ServiceException e) {
		List<ParameterizedMessage> pMsgs = e.getMessages();
		for (ParameterizedMessage pMsg : pMsgs) {
			switch (pMsg.getMsgType()) {
				case ERROR:
					logger.error(MessageFormat.format(pMsg.getMsgCode(), pMsg.getMsgParameters()), e);
					break;
				case WARNING:
					logger.warn(MessageFormat.format(pMsg.getMsgCode(), pMsg.getMsgParameters()), e);
					break;
				case INFO:
					logger.info(MessageFormat.format(pMsg.getMsgCode(), pMsg.getMsgParameters()), e);
					break;
				default:
					break;
			}
		}
	}

}
