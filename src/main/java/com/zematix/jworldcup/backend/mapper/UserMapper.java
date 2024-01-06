package com.zematix.jworldcup.backend.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.zematix.jworldcup.backend.dto.UserDto;
import com.zematix.jworldcup.backend.entity.Dictionary;
import com.zematix.jworldcup.backend.entity.User;

@Mapper
public interface UserMapper extends MapperBase<UserDto, User> {

    @Override
    @Mapping(source = "userStatus.value", target = "userStatus", ignore = true)
    @Mapping(source = "loginPassword", target = "loginPassword", ignore = true) // for security reasons
    @Mapping(source = "roles", target = "authorities")
    UserDto entityToDto(User user);

    @Override
    @Mapping(source = "userStatus", target = "userStatus.value", ignore = true)
    @Mapping(source = "loginPassword", target = "loginPassword", ignore = true) // for security reasons
    User dtoToEntity(UserDto userDto);
	
	@Mapping(source = "user.userStatus.value", target = "userStatus")
	@Mapping(source = "user.loginPassword", target = "loginPassword", ignore = true) // for security reasons
	@Mapping(source = "authorities", target = "authorities")
	UserDto entityToDto(User user, Set<String> authorities);

	default Set<String> mapAuthorities(Set<Dictionary> roles) {
		return roles == null ? null : roles.stream().map(e -> "ROLE_" + e.getValue()).collect(Collectors.toSet());
	}
	
}
