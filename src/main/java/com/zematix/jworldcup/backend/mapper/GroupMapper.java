package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;

import com.zematix.jworldcup.backend.dto.GroupDto;
import com.zematix.jworldcup.backend.entity.Group;

@Mapper
public interface GroupMapper extends MapperBase<GroupDto, Group> {

}
