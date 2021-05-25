package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;

import com.zematix.jworldcup.backend.dto.UserOfEventDto;
import com.zematix.jworldcup.backend.entity.UserOfEvent;

@Mapper
public interface UserOfEventMapper extends MapperBase<UserOfEventDto, UserOfEvent> {

}
