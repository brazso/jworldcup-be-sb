package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;

import com.zematix.jworldcup.backend.dto.UserPositionDto;
import com.zematix.jworldcup.backend.model.UserPosition;

@Mapper
public interface UserPositionMapper extends MapperBase<UserPositionDto, UserPosition> {

}
