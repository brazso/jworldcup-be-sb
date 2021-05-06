package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;

import com.zematix.jworldcup.backend.dto.UserDto;
import com.zematix.jworldcup.backend.entity.User;

@Mapper
public interface UserMapper extends MapperBase<UserDto, User> {

//    @Override
//    @Mapping(source = "userStatus.status", target = "userStatus")
//    UserDto entityToDto(User user);

//    @Override
//    @Mapping(source = "userStatus", target = "userStatus.status", ignore = true)
//    User dtoToEntity(UserDto userDto);
}
