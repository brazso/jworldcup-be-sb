package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.zematix.jworldcup.backend.dto.SessionInfo;
import com.zematix.jworldcup.backend.dto.SessionInfoDto;

@Mapper(uses = {EventMapper.class, UserMapper.class, UserOfEventMapper.class})
public interface SessionInfoMapper extends MapperBase<SessionInfoDto, SessionInfo> {

	@Mapping(target = "localeId", expression = "java( sessionInfo.getLocale() == null ? null : sessionInfo.getLocale().toLanguageTag() )")
	SessionInfoDto entityToDto(SessionInfo sessionInfo);

	@Mapping(target = "locale", expression = "java( sessionInfoDto.getLocaleId() == null ? null : java.util.Locale.forLanguageTag( sessionInfoDto.getLocaleId() ) )") 
	SessionInfo dtoToEntity(SessionInfoDto sessionInfoDto);

}
