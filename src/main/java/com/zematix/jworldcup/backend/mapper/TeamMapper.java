package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;

import com.zematix.jworldcup.backend.dto.TeamDto;
import com.zematix.jworldcup.backend.entity.Team;

@Mapper
public interface TeamMapper extends MapperBase<TeamDto, Team> {

}
