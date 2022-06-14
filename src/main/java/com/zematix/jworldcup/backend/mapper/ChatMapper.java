package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.zematix.jworldcup.backend.dto.ChatDto;
import com.zematix.jworldcup.backend.entity.Chat;

@Mapper(uses = {UserMapper.class, UserGroupMapper.class})
public interface ChatMapper extends MapperBase<ChatDto, Chat> {

	@Override
	@Mapping(target = "private", expression = "java( chat.isPrivateAsBoolean() )")
	ChatDto entityToDto(Chat chat);

	@Override
	@Mapping(target = "private", expression = "java( chatDto.isPrivate() ? (byte) 1 : (byte) 0 )") 
	Chat dtoToEntity(ChatDto chatDto);

}
