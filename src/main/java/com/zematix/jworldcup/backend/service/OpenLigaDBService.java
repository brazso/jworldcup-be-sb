package com.zematix.jworldcup.backend.service;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Service;

import com.msiggi.openligadb.model.Group;
import com.msiggi.openligadb.model.League;
import com.msiggi.openligadb.model.Match;
import com.msiggi.openligadb.model.Sport;
import com.msiggi.openligadb.model.Team;
import com.zematix.jworldcup.backend.exception.OpenLigaDBException;

/**
 * Web service client implementation to get online results of football matches 
 * from <a href="https://www.openligadb.de">OpenLigaDB</a>. In the beginning
 * OpenLigaDB had supported SOAP methods only, but later it was expanded to REST ones.
 * Because technically both are supported yet, JWorldcup app still calls old SOAP methods 
 * but also new REST ones. The latter ones are those which were not implemented 
 * at OpenLigaDB/SOAP, they can be called only by REST interface.
 * Note: in fact all SOAP calls should be transferred to REST ones later, but 
 * OpenLigaDB REST interface is not so well documented, there is no Swagger support 
 * either.
 */
@Service
public class OpenLigaDBService extends ServiceBase {
	
	public static final String OPENLIGADB_WEB_API_URI = "https://api.openligadb.de";

	/**
	 * Service wrapper of the {@link SportsDataSoap#getAvailSports()} method
	 * 
	 * @return list of available sport types
	 * @throws OpenLigaDBException if there is problem with the WS call
	 */
	public List<Sport> getAvailableSports() throws OpenLigaDBException {
		List<Sport> sports = new ArrayList<>();
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(OPENLIGADB_WEB_API_URI + "/getavailablesports");
		try {
			Builder builder = target.request(MediaType.APPLICATION_JSON);
			sports = builder.get(new GenericType<List<Sport>>() {});
		}
		catch (Exception e) {
			throw new OpenLigaDBException(e.getMessage());
		}
		return sports;
	}
	
	/**
	 * Service wrapper of the {@link SportsDataSoap#getAvailableLeagues()} method
	 * 
	 * @return list of all leagues
	 * @throws OpenLigaDBException if there is problem with the WS call
	 */
	public List<League> getAvailableLeagues() throws OpenLigaDBException {
		List<League> leagues = new ArrayList<>();
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(OPENLIGADB_WEB_API_URI + "/getavailableleagues");
		try {
			Builder builder = target.request(MediaType.APPLICATION_JSON);
			leagues = builder.get(new GenericType<List<League>>() {});
		}
		catch (Exception e) {
			throw new OpenLigaDBException(e.getMessage());
		}
		return leagues;
	}
	
	/**
	 * Service wrapper of the OpenLigaDB WEB-API getavailablegroups method
	 * 
	 * @return list of available groups of a season (event)
	 * @throws OpenLigaDBException if there is problem with the WS call
	 */
	public List<Group> getAvailableGroups(String leagueShortcut, String leagueSeason) throws OpenLigaDBException {
		List<Group> groups = new ArrayList<>();
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(OPENLIGADB_WEB_API_URI + "/getavailablegroups/{leagueShortcut}/{leagueSeason}")
				.resolveTemplate("leagueShortcut", leagueShortcut)
				.resolveTemplate("leagueSeason", leagueSeason);
		try {
			Builder builder = target.request(MediaType.APPLICATION_JSON);
			groups = builder.get(new GenericType<List<Group>>() {});
		}
		catch (Exception e) {
			throw new OpenLigaDBException(e.getMessage());
		}
		return groups;
	}

	/**
	 * Service wrapper of the OpenLigaDB WEB-API getmatchdata method
	 * Note: this method is not supported by SOAP methods, that is why REST is used
	 * 
	 * @return list of available matches of a season (event)
	 * @throws OpenLigaDBException if there is problem with the WS call
	 */
	public List<Match> getMatchdata(String leagueShortcut, String leagueSeason) throws OpenLigaDBException {
		List<Match> matchdatas = new ArrayList<>();
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(OPENLIGADB_WEB_API_URI + "/getmatchdata/{leagueShortcut}/{leagueSeason}")
				.resolveTemplate("leagueShortcut", leagueShortcut)
				.resolveTemplate("leagueSeason", leagueSeason);
		try {
			Builder builder = target.request(MediaType.APPLICATION_JSON);
			matchdatas = builder.get(new GenericType<List<Match>>() {});
		}
		catch (Exception e) {
			throw new OpenLigaDBException(e.getMessage());
		}
		return matchdatas;
	}

	/**
	 * Service wrapper of the OpenLigaDB WEB-API getavailableteams method
	 * Note: this method is not supported by SOAP methods, that is why REST is used
	 * 
	 * @return list of all teams of a season (event)
	 * @throws OpenLigaDBException if there is problem with the WS call
	 */
	public List<Team> getAvailableTeams(String leagueShortcut, String leagueSeason) throws OpenLigaDBException {
		List<Team> teams = new ArrayList<>();
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(OPENLIGADB_WEB_API_URI + "/getavailableteams/{leagueShortcut}/{leagueSeason}")
				.resolveTemplate("leagueShortcut", leagueShortcut)
				.resolveTemplate("leagueSeason", leagueSeason);
		try {
			Builder builder = target.request(MediaType.APPLICATION_JSON);
			teams = builder.get(new GenericType<List<Team>>() {});
		}
		catch (Exception e) {
			throw new OpenLigaDBException(e.getMessage());
		}
		return teams;
	}
}
