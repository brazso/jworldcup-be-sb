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

import com.msiggi.openligadb.client.ArrayOfMatchdata;
import com.msiggi.openligadb.client.ArrayOfSport;
import com.msiggi.openligadb.client.Matchdata;
import com.msiggi.openligadb.client.Sport;
import com.msiggi.openligadb.client.Sportsdata;
import com.zematix.jworldcup.backend.exception.OpenLigaDBException;
import com.zematix.jworldcup.backend.model.openligadb.client.Team;

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
	
	public static final String OPENLIGADB_WEB_API_URI = "https://www.openligadb.de/api/";

	/**
	 * Service wrapper of the {@link SportsDataSoap#getAvailSports()} method
	 * 
	 * @return list of available sport types
	 * @throws OpenLigaDBException if there is problem with the WS call
	 */
	public List<Sport> getAvailableSports() throws OpenLigaDBException {
		List<Sport> sports = new ArrayList<>();
		try {
			Sportsdata sportsdata = new Sportsdata();
			ArrayOfSport aos = sportsdata.getSportsdataSoap12().getAvailSports();
			/*List<Sport>*/ sports = aos.getSport();
		}
		catch (/*WebService*/Exception e) {
			throw new OpenLigaDBException(e.getMessage());
		}
		return sports;
	}
	
	/**
	 * Service wrapper of the {@link SportsDataSoap#getMatchdataByLeagueSaison(String, String)} method
	 * 
	 * @return list of available matches of a season (event)
	 * @throws OpenLigaDBException if there is problem with the WS call
	 */
	public List<Matchdata> getMatchdataByLeagueSaison(String leagueShortcut, String leagueSaison) throws OpenLigaDBException {
		List<Matchdata> matchdatas = new ArrayList<>();
		try {
			Sportsdata sportsdata = new Sportsdata();
			ArrayOfMatchdata aomd = sportsdata.getSportsdataSoap12().getMatchdataByLeagueSaison(leagueShortcut, leagueSaison);
			/*List<Sport>*/ matchdatas = aomd.getMatchdata();
		}
		catch (/*WebService*/Exception e) {
			throw new OpenLigaDBException(e.getMessage());
		}
		return matchdatas;
	}

	/**
	 * Service wrapper of the OpenLigaDB WEB-API getmatchdata method
	 * Note: this method is not supported by SOAP methods, that is why REST is used
	 * 
	 * @return list of available matches of a season (event)
	 * @throws OpenLigaDBException if there is problem with the WS call
	 */
	public List<com.zematix.jworldcup.backend.model.openligadb.client.Matchdata> getMatchdata(String leagueShortcut, String leagueSaison) throws OpenLigaDBException {
		List<com.zematix.jworldcup.backend.model.openligadb.client.Matchdata> matchdatas = new ArrayList<>();
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(OPENLIGADB_WEB_API_URI + "/getmatchdata/{leagueShortcut}/{leagueSaison}")
				.resolveTemplate("leagueShortcut", leagueShortcut)
				.resolveTemplate("leagueSaison", leagueSaison);
		try {
			Builder builder = target.request(MediaType.APPLICATION_JSON);
			matchdatas = builder.get(new GenericType<List<com.zematix.jworldcup.backend.model.openligadb.client.Matchdata>>() {});
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
	public List<Team> getAvailableTeams(String leagueShortcut, String leagueSaison) throws OpenLigaDBException {
		List<Team> teams = new ArrayList<>();
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(OPENLIGADB_WEB_API_URI + "/getavailableteams/{leagueShortcut}/{leagueSaison}")
				.resolveTemplate("leagueShortcut", leagueShortcut)
				.resolveTemplate("leagueSaison", leagueSaison);
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
