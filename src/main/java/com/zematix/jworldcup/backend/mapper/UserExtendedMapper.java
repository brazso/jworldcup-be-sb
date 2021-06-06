package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.zematix.jworldcup.backend.dto.UserExtendedDto;
import com.zematix.jworldcup.backend.model.UserExtended;

@Mapper
public interface UserExtendedMapper extends MapperBase<UserExtendedDto, UserExtended> {

	@Mapping(source = "userStatus.status", target = "userStatus", ignore = true)
	@Mapping(source = "locale", target = "languageTag", ignore = true)
	UserExtendedDto entityToDto(UserExtended userExtended);

	@Mapping(source = "userStatus", target = "userStatus.status", ignore = true)
	@Mapping(target = "locale", expression = "java( userExtendedDto.getLanguageTag() == null ? null : java.util.Locale.forLanguageTag( userExtendedDto.getLanguageTag() ) )") 
	UserExtended dtoToEntity(UserExtendedDto userExtendedDto);

//	default Set<String> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
//		return authorities.stream().map(e -> e.getAuthority()).collect(Collectors.toSet());
//    }
//
//	default Collection<? extends GrantedAuthority> mapAuthorities(Set<String> authorities) {
//		return authorities.stream().map(e -> new SimpleGrantedAuthority(/*"ROLE_" +*/ e)).collect(Collectors.toSet());
//    }

}
