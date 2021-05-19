package com.zematix.jworldcup.backend.dto;

import com.zematix.jworldcup.backend.emun.ParameterizedMessageType;
import com.zematix.jworldcup.backend.exception.ApiErrorItem;

import lombok.Data;

@Data 
public class ParameterizedMessageDto implements ApiErrorItem {

	private String msgCode;
	private ParameterizedMessageType msgType;
	private Object[] msgParams = new Object[]{};
	private String msgBuilt;
}
