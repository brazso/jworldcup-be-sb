package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.zematix.jworldcup.backend.dto.SessionDataDto;
import com.zematix.jworldcup.backend.model.SessionData;

@Mapper(uses = {EventMapper.class, UserMapper.class, UserOfEventMapper.class, UserGroupMapper.class})
public interface SessionDataMapper extends MapperBase<SessionDataDto, SessionData> {

	@Mapping(target = "localeId", expression = "java( sessionData.getLocale() == null ? null : sessionData.getLocale().toLanguageTag() )")
	SessionDataDto entityToDto(SessionData sessionData);

	@Mapping(target = "locale", expression = "java( sessionDataDto.getLocaleId() == null ? null : java.util.Locale.forLanguageTag( sessionDataDto.getLocaleId() ) )") 
	SessionData dtoToEntity(SessionDataDto sessionDataDto);

}
