package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.zematix.jworldcup.backend.dto.UserGroupExtendedDto;
import com.zematix.jworldcup.backend.model.UserGroupExtended;

@Mapper(uses = {UserMapper.class})
public interface UserGroupExtendedMapper extends MapperBase<UserGroupExtendedDto, UserGroupExtended> {

	@Override
	@Mapping(target = "isPublicEditable", expression = "java( userGroupExtended.isPublicEditableAsBoolean() )")
	@Mapping(target = "isPublicVisible", expression = "java( userGroupExtended.isPublicVisibleAsBoolean() )")
	UserGroupExtendedDto entityToDto(UserGroupExtended userGroupExtended);

	@Override
	@Mapping(target = "isPublicEditable", expression = "java( userGroupExtendedDto.getIsPublicEditable() == null ? null : (userGroupExtendedDto.getIsPublicEditable().booleanValue() ? (byte) 1 : (byte) 0) )") 
	@Mapping(target = "isPublicVisible", expression = "java( userGroupExtendedDto.getIsPublicVisible() == null ? null : (userGroupExtendedDto.getIsPublicVisible().booleanValue() ? (byte) 1 : (byte) 0) )")
	UserGroupExtended dtoToEntity(UserGroupExtendedDto userGroupExtendedDto);

}
