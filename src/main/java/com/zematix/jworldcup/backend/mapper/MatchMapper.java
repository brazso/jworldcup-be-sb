package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;

import com.zematix.jworldcup.backend.dto.MatchDto;
import com.zematix.jworldcup.backend.entity.Match;

@Mapper(uses=RoundMapper.class)
public interface MatchMapper extends MapperBase<MatchDto, Match> {
}
