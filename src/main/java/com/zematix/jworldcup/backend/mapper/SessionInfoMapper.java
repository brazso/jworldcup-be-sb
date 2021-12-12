package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;

import com.zematix.jworldcup.backend.dto.SessionInfo;
import com.zematix.jworldcup.backend.dto.SessionInfoDto;

@Mapper(uses = {EventMapper.class, UserMapper.class, UserOfEventMapper.class})
public interface SessionInfoMapper extends MapperBase<SessionInfoDto, SessionInfo> {

}
