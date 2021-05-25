package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;

import com.zematix.jworldcup.backend.dto.EventDto;
import com.zematix.jworldcup.backend.entity.Event;

@Mapper
public interface EventMapper extends MapperBase<EventDto, Event> {

}
