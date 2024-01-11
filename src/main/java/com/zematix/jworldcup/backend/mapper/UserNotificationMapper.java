package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;

import com.zematix.jworldcup.backend.dto.DictionaryDto;
import com.zematix.jworldcup.backend.dto.UserNotificationDto;
import com.zematix.jworldcup.backend.entity.UserNotification;

@Mapper(uses = {UserMapper.class, DictionaryDto.class})
public interface UserNotificationMapper extends MapperBase<UserNotificationDto, UserNotification> {

	@Override
	UserNotificationDto entityToDto(UserNotification userNotification);

	@Override
	UserNotification dtoToEntity(UserNotificationDto userNotificationDto);
}
