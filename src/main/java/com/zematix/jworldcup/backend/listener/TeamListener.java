package com.zematix.jworldcup.backend.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.persistence.PostLoad;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Configurable;

import com.zematix.jworldcup.backend.configuration.QuartzConfig;
import com.zematix.jworldcup.backend.configuration.StaticContextAccessor;
import com.zematix.jworldcup.backend.dao.DictionaryDao;
import com.zematix.jworldcup.backend.emun.DictionaryEnum;
import com.zematix.jworldcup.backend.entity.Dictionary;
import com.zematix.jworldcup.backend.entity.Team;

/**
 * Transient fields of {@link Team} are initialized here. 
 */
@Configurable
public class TeamListener {

	/**
	 * Dependencies like {@link DictionaryDao} cannot be injected
	 * inside @EntityListeners classes. {@link StaticContextAccessor} cannot be used
	 * to retrieve dependency either because {@link QuartzConfig#init()} calls it
	 * before its constructor injection.
	 */
	@Inject
	private ObjectFactory<DictionaryDao> dictionaryDaoProvider;
	
	@PostLoad
	public void initialize(Team team) {
		team.setWsIds(getWsIds(team.getWsId()));
	}
	
	private List<Long> getWsIds(Long wsId) {
		List<Long> wsIds = new ArrayList<>();
		if (wsId != null) {
			wsIds.add(wsId);
			DictionaryDao dictionaryDao = dictionaryDaoProvider.getObject();
			Dictionary dictionary = dictionaryDao.findDictionaryByKeyAndValue(DictionaryEnum.WS_TEAM_ID.name(), Long.toString(wsId));
			if (dictionary != null) {
				wsIds.addAll(Arrays.asList(dictionary.getName().split(",")).stream().map(Long::parseLong).toList());
			}
		}
		return wsIds;
	}
}
