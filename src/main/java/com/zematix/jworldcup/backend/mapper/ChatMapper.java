package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;

import com.zematix.jworldcup.backend.dto.ChatDto;
import com.zematix.jworldcup.backend.entity.Chat;

@Mapper
public interface ChatMapper extends MapperBase<ChatDto, Chat> {

}
