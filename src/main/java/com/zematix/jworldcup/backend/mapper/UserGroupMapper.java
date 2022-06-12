package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.zematix.jworldcup.backend.dto.UserGroupDto;
import com.zematix.jworldcup.backend.entity.UserGroup;

@Mapper(uses = {UserMapper.class})
public interface UserGroupMapper extends MapperBase<UserGroupDto, UserGroup> {

	@Override
	@Mapping(target = "isPublicEditable", expression = "java( userGroup.isPublicEditableAsBoolean() )")
	@Mapping(target = "isPublicVisible", expression = "java( userGroup.isPublicVisibleAsBoolean() )")
	UserGroupDto entityToDto(UserGroup userGroup);

	@Override
	@Mapping(target = "isPublicEditable", expression = "java( userGroupDto.getIsPublicEditable() == null ? null : (userGroupDto.getIsPublicEditable().booleanValue() ? (byte) 1 : (byte) 0) )") 
	@Mapping(target = "isPublicVisible", expression = "java( userGroupDto.getIsPublicVisible() == null ? null : (userGroupDto.getIsPublicVisible().booleanValue() ? (byte) 1 : (byte) 0) )")
	UserGroup dtoToEntity(UserGroupDto userGroupDto);

	@AfterMapping
	default void map(UserGroup userGroup, @MappingTarget UserGroupDto userGroupDto) {
		userGroup.getVirtualUsers().forEach(user -> {
			user.setEmailAddr(null); // for security reasons
		});
	}
}
