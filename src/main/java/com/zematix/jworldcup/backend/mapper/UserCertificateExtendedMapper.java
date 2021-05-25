package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.zematix.jworldcup.backend.dto.UserCertificateExtendedDto;
import com.zematix.jworldcup.backend.model.UserCertificateExtended;

@Mapper
public interface UserCertificateExtendedMapper extends MapperBase<UserCertificateExtendedDto, UserCertificateExtended> {

	@Override
	@Mapping(source = "locale", target = "languageTag", ignore = true)
	UserCertificateExtendedDto entityToDto(UserCertificateExtended userCertificateExtended);

	@Override
	@Mapping(target = "locale", expression = "java( userCertificateExtendedDto.getLanguageTag() == null ? null : java.util.Locale.forLanguageTag( userCertificateExtendedDto.getLanguageTag() ) )") 
	UserCertificateExtended dtoToEntity(UserCertificateExtendedDto userCertificateExtendedDto);

}
