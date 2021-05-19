package com.zematix.jworldcup.backend.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.context.MessageSource;

import com.zematix.jworldcup.backend.configuration.StaticContextAccessor;
import com.zematix.jworldcup.backend.dto.ParameterizedMessageDto;
import com.zematix.jworldcup.backend.model.ParameterizedMessage;

/**
 * Mapper from {@link ParameterizedMessage} to {@link ParameterizedMessageDto}.
 * Because this class might be instantiated manually, injection of other
 * classes cannot be used inside.
 */
@Mapper
public abstract class ParameterizedMessageMapper {

	private final MessageSource msgs = StaticContextAccessor.getBean(MessageSource.class);
	
	//@Mapping(target = "msgBuilt", expression = "java( parameterizedMessage.buildMessage() )")
	@Mapping(target = "msgBuilt", source = "parameterizedMessage")
	public abstract ParameterizedMessageDto entityToDto(ParameterizedMessage parameterizedMessage);
	
	public abstract List<ParameterizedMessageDto> entityListToDtoList(List<ParameterizedMessage> entityList);

	/**
	 * Builds legible message from the given parameterized message.
	 * @param parameterizedMessage
	 * @return legible message
	 */
	String mapMsgBuilt(ParameterizedMessage parameterizedMessage) {
		return parameterizedMessage.buildMessage(msgs);
    }
}
