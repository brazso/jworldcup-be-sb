package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.zematix.jworldcup.backend.dto.RoundDto;
import com.zematix.jworldcup.backend.entity.Round;

@Mapper
public interface RoundMapper extends MapperBase<RoundDto, Round> {

	@Override
	@Mapping(target = "isGroupmatch", expression = "java( round.getIsGroupmatchAsBoolean() )")
	@Mapping(target = "isOvertime", expression = "java( round.getIsOvertimeAsBoolean() )")
	RoundDto entityToDto(Round round);

	@Override
	@Mapping(target = "isGroupmatch", expression = "java( roundDto.getIsGroupmatch() == null ? null : (roundDto.getIsGroupmatch().booleanValue() ? (byte) 1 : (byte) 0) )") 
	@Mapping(target = "isOvertime", expression = "java( roundDto.getIsOvertime() == null ? null : (roundDto.getIsOvertime().booleanValue() ? (byte) 1 : (byte) 0) )")
	Round dtoToEntity(RoundDto roundDto);

}
