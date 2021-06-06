package com.zematix.jworldcup.backend.mapper;

import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.zematix.jworldcup.backend.dto.UserDto;
import com.zematix.jworldcup.backend.entity.User;

@Mapper
public interface UserMapper extends MapperBase<UserDto, User> {

    @Override
    @Mapping(source = "userStatus.status", target = "userStatus", ignore = true)
    UserDto entityToDto(User user);

    @Override
    @Mapping(source = "userStatus", target = "userStatus.status", ignore = true)
    User dtoToEntity(UserDto userDto);
	
	@Mapping(source = "authorities", target = "authorities")
	@Mapping(source = "user.userStatus.status", target = "userStatus")
	UserDto entityToDto(User user, Set<String> authorities);

}
