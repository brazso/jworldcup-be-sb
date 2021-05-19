package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;

import com.zematix.jworldcup.backend.dto.BetDto;
import com.zematix.jworldcup.backend.entity.Bet;

@Mapper
public interface BetMapper extends MapperBase<BetDto, Bet> {

}
