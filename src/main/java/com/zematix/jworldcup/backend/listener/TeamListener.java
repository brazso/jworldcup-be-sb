package com.zematix.jworldcup.backend.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.PostLoad;

import com.zematix.jworldcup.backend.configuration.StaticContextAccessor;
import com.zematix.jworldcup.backend.dao.DictionaryDao;
import com.zematix.jworldcup.backend.emun.DictionaryEnum;
import com.zematix.jworldcup.backend.entity.Dictionary;
import com.zematix.jworldcup.backend.entity.Team;

/**
 * Transient fields of {@link TeamListen} are initialized here.
 * Dependencies like {@link DictionaryDao} cannot be injected as usual. 
 */
public class TeamListener {
	
	@PostLoad
	public void initialize(Team team) {
		team.setWsIds(getWsIds(team.getWsId()));
	}
	
	private List<Long> getWsIds(Long wsId) {
		List<Long> wsIds = new ArrayList<>();
		if (wsId != null) {
			wsIds.add(wsId);
			DictionaryDao dictionaryDao = StaticContextAccessor.getBean(DictionaryDao.class);
			Dictionary dictionary = dictionaryDao.findDictionaryByKeyAndValue(DictionaryEnum.WS_TEAM_ID.name(), Long.toString(wsId));
			if (dictionary != null) {
				wsIds.addAll(Arrays.asList(dictionary.getName().split(",")).stream().map(Long::parseLong).toList());
			}
		}
		return wsIds;
	}
}
