package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;

import com.zematix.jworldcup.backend.dto.MatchDto;
import com.zematix.jworldcup.backend.entity.Match;

@Mapper
public interface MatchMapper extends MapperBase<MatchDto, Match> {

//	@Override
//	@Mapping(target = "isGroupmatch", expression = "java( round.getIsGroupmatchAsBoolean() )")
//	@Mapping(target = "isOvertime", expression = "java( round.getIsOvertimeAsBoolean() )")
//	RoundDto entityToDto(Round round);
//
//	@Override
//	@Mapping(target = "isGroupmatch", expression = "java( roundDto.getIsGroupmatch() == null ? null : (roundDto.getIsGroupmatch().booleanValue() ? (byte) 1 : (byte) 0) )") 
//	@Mapping(target = "isOvertime", expression = "java( roundDto.getIsOvertime() == null ? null : (roundDto.getIsOvertime().booleanValue() ? (byte) 1 : (byte) 0) )")
//	Round dtoToEntity(RoundDto roundDto);

}
