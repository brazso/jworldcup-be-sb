package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.zematix.jworldcup.backend.dto.UserGroupDto;
import com.zematix.jworldcup.backend.entity.UserGroup;

/**
 * Equals to {@link UserGroupMapper}, but it ignores {@link UserGroup#users} field. 
 * @author brazso
 */
@Mapper(uses = {EventMapper.class, UserMapper.class})
public interface UserGroupOfChatMapper extends MapperBase<UserGroupDto, UserGroup> {

	@Override
	@Mapping(target = "isPublicEditable", expression = "java( userGroup.isPublicEditableAsBoolean() )")
	@Mapping(target = "isPublicVisible", expression = "java( userGroup.isPublicVisibleAsBoolean() )")
	@Mapping(target = "users", ignore = true)
	UserGroupDto entityToDto(UserGroup userGroup);

	@Override
	@Mapping(target = "isPublicEditable", expression = "java( userGroupDto.getIsPublicEditable() == null ? null : (userGroupDto.getIsPublicEditable().booleanValue() ? (byte) 1 : (byte) 0) )") 
	@Mapping(target = "isPublicVisible", expression = "java( userGroupDto.getIsPublicVisible() == null ? null : (userGroupDto.getIsPublicVisible().booleanValue() ? (byte) 1 : (byte) 0) )")
	@Mapping(target = "users", ignore = true)
	UserGroup dtoToEntity(UserGroupDto userGroupDto);

}
