package com.zematix.jworldcup.backend.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.zematix.jworldcup.backend.dto.UserDto;
import com.zematix.jworldcup.backend.entity.Role;
import com.zematix.jworldcup.backend.entity.User;

@Mapper
public interface UserMapper extends MapperBase<UserDto, User> {

    @Override
    @Mapping(source = "userStatus.status", target = "userStatus", ignore = true)
    @Mapping(source = "loginPassword", target = "loginPassword", ignore = true) // for security reasons
    @Mapping(source = "roles", target = "authorities")
    UserDto entityToDto(User user);

    @Override
    @Mapping(source = "userStatus", target = "userStatus.status", ignore = true)
    @Mapping(source = "loginPassword", target = "loginPassword", ignore = true) // for security reasons
    User dtoToEntity(UserDto userDto);
	
	@Mapping(source = "user.userStatus.status", target = "userStatus")
	@Mapping(source = "user.loginPassword", target = "loginPassword", ignore = true) // for security reasons
	@Mapping(source = "authorities", target = "authorities")
	UserDto entityToDto(User user, Set<String> authorities);

	default Set<String> mapAuthorities(Set<Role> roles) {
		return roles.stream().map(e -> "ROLE_" + e.getRole()).collect(Collectors.toSet());
	}
	
}
