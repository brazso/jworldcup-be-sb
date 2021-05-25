package com.zematix.jworldcup.backend.mapper;

import org.mapstruct.Mapper;

import com.zematix.jworldcup.backend.dto.GroupTeamDto;
import com.zematix.jworldcup.backend.model.GroupTeam;

@Mapper(uses = {TeamMapper.class, MatchMapper.class})
public interface GroupTeamMapper extends MapperBase<GroupTeamDto, GroupTeam> {

}
