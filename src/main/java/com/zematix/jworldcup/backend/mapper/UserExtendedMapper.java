package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.zematix.jworldcup.backend.dto.UserExtendedDto;
import com.zematix.jworldcup.backend.entity.model.UserExtended;

@Mapper
public interface UserExtendedMapper extends MapperBase<UserExtendedDto, UserExtended> {

	@Override
	@Mapping(source = "locale", target = "languageTag", ignore = true)
	UserExtendedDto entityToDto(UserExtended userExtended);

	@Override
	@Mapping(target = "locale", expression = "java( userExtendedDto.getLanguageTag() == null ? null : java.util.Locale.forLanguageTag( userExtendedDto.getLanguageTag() ) )") 
	UserExtended dtoToEntity(UserExtendedDto userExtendedDto);

}
