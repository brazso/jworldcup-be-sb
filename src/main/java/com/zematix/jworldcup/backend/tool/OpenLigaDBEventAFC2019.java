package com.zematix.jworldcup.backend.tool;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.msiggi.openligadb.model.League;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Group;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.Round;
import com.zematix.jworldcup.backend.entity.Team;
import com.zematix.jworldcup.backend.entity.WebService;
import com.zematix.jworldcup.backend.exception.OpenLigaDBException;
import com.zematix.jworldcup.backend.util.CommonUtil;

/**
 * Imports complete AFC2019 event from OpenLigaDB. Groups come from the matches 
 * of the 1st round, namely from its location. Having run this script on 06-12-2018,
 * there was no need to post-database work. There were no new team icons neither.
 */
@Deprecated
public class OpenLigaDBEventAFC2019 extends OpenLigaDBEvent {

	public OpenLigaDBEventAFC2019() {
		
	}

	/**
	 * Imports event belongs to {@link OpenLigaDBEventAFC2019}
	 * 
	 * @return {@code true} if the modifications are commitable, {@code false} otherwise
	 */
	@Override
	public boolean importEvent() throws OpenLigaDBException {
		EntityManager em = (EntityManager) params.get("EntityManager");
		checkNotNull(em, "Parameter named EntityManager is not set, its value cannot be null.");
		
		final String EVENT_LOCATION = "United Arab Emirates";
		final String EVENT_DESCRIPTION = "Asian Cup";
		final String EVENT_SHORT_DESC = "AFC";
		final Short EVENT_YEAR = 2019;
		final String EVENT_ORGANIZER = "AFC";
		final String LEAGUE_SHORTCUT = "AFC2019";
		final String LEAGUE_SEASON = "2019";
		final List<String> ORDNUNGZAHLEN = Arrays.asList("Sieger", "Zweiter", "Dritter");
		
		Map<String, String> fifaCodeByCountryNameMap = retrieveFifaCodeByCountryNameMap();
		checkNotNull(fifaCodeByCountryNameMap, "Retrieved fifaCodeByCountryNameMap cannot be null.");

		List<League> oldbLeagues = openLigaDBService.getAvailableLeagues();
		League league = oldbLeagues.stream()
				.filter(e -> LEAGUE_SHORTCUT.equals(e.getLeagueShortcut()) && LEAGUE_SEASON.equals(e.getLeagueSeason()))
				.findFirst().orElse(null);
		if (league == null) {
			String msg = String.format("League is not found in OpenLigaDB where "
					+ "leagueShortcut=%s and leagueSaison=%s.",
					LEAGUE_SHORTCUT, LEAGUE_SEASON);
			logger.error(msg);
			throw new OpenLigaDBException(msg);
		}
		// 4329, Fußball-Asienmeisterschaft 2019, 2019, AFC2019
		
		Event event = new Event();
		event.setLocation(EVENT_LOCATION);
		event.setYear(EVENT_YEAR);
		event.setDescription(EVENT_DESCRIPTION);
		event.setShortDesc(EVENT_SHORT_DESC);
		event.setOrganizer(EVENT_ORGANIZER);
		em.persist(event);
		
		List<com.msiggi.openligadb.model.Group> oldbGroups = openLigaDBService.getAvailableGroups(LEAGUE_SHORTCUT, LEAGUE_SEASON);
//		33409, 1. Runde, 1
//		33410, 2. Runde, 2
//		33411, 3. Runde, 3
//		33412, Achtelfinale, 4
//		33413, Viertelfinale, 5
//		33414, Halbfinale, 6
//		33415, Finale, 7

		Map<Integer, Round> roundMap = new HashMap<>();
		List<Round> roundList = new ArrayList<>();
		for (com.msiggi.openligadb.model.Group oldbGroup : oldbGroups) {
			Round round = new Round();
			round.setEvent(event);
			String name = oldbGroup.getGroupName();
			boolean isGroupMatch = false;
			switch (name) {
				case "1. Runde":
					name = "1st round";
					isGroupMatch = true;
					break;
				case "2. Runde":
					name = "2nd round";
					isGroupMatch = true;
					break;
				case "3. Runde":
					name = "3rd round";
					isGroupMatch = true;
					break;
				case "Achtelfinale":
					name = "Round of 16";
					isGroupMatch = false;
					break;
				case "Viertelfinale":
					name = "Quarter-finals";
					isGroupMatch = false;
					break;
				case "Halbfinale":
					name = "Semi-finals";
					isGroupMatch = false;
					break;
				case "Spiel um Platz 3": // not used by AFC2019
					name = "Third place play-off";
					isGroupMatch = false;
					break;
				case "Finale":
					name = "Final";
					isGroupMatch = false;
					break;
				default:
					String msg = String.format("Unsupported OpenLigaDB \"%s\" group name found. "
							+ "Update the import script.", name);
					logger.error(msg);
					throw new OpenLigaDBException(msg);
			}
			round.setName(name);
			round.setIsGroupmatchAsBoolean(isGroupMatch);
			em.persist(round);
			roundMap.put(oldbGroup.getGroupID(), round);
			roundList.add(round);
		}
		
		TypedQuery<Team> query = em.createNamedQuery("Team.findAll", Team.class);
		List<Team> teams = query.getResultList();
		
		Map<String, Team> teamMap = 
				teams
					.stream()
					.sorted((t1, t2) -> t1.getName().compareTo(t2.getName()))
					.filter(distinctByKey(Team::getName))
					.collect(Collectors.toMap(t -> t.getName(), t -> t));

		List<com.msiggi.openligadb.model.Team> oldbTeams = openLigaDBService.getAvailableTeams(LEAGUE_SHORTCUT, LEAGUE_SEASON);
		
		// among teams there are Gruppe, Sieger, Verlierer ones. The latter ones have no icon.
		List<com.msiggi.openligadb.model.Team> oldbRealTeams = 
				oldbTeams.stream().filter(e->!e.getTeamIconUrl().isEmpty()).toList();
		
		List<Group> groupList = new ArrayList<>();
		for (int i=0; i < oldbRealTeams.size()/4; i++) {
			Group group = new Group();
			group.setEvent(event);
			group.setName(String.valueOf((char)('A'+i)));
			em.persist(group);
			groupList.add(group);
		}
		
		Map<Integer, Team> teamMapByWsId = new HashMap<>();
		for (com.msiggi.openligadb.model.Team oldbTeam : oldbRealTeams) {
			Team team = new Team();
			team.setEvent(event);
			boolean isLowerCase = false;
			// url #1 (mostly used) contains English name, it can be used for translation
			// e.g. https://upload.wikimedia.org/wikipedia/commons/thumb/5/53/Flag_of_Syria.svg/20px-Flag_of_Syria.svg.png
			Matcher matcher = Pattern.compile("(.*?_of_)(.*?)(_%28.*|\\.svg.*)").matcher(oldbTeam.getTeamIconUrl());
			if (!matcher.find()) {
				// url #2 (rarely used) contains English name, it can be used for translation
				// e.g. http://www.nationalflaggen.de/media/flags/flagge-thailand.gif
				matcher = Pattern.compile("(.*?flagge-)(.*?)(\\..*)").matcher(oldbTeam.getTeamIconUrl());
				isLowerCase = true;
			} else {
				matcher.reset();
			}
			String name; 
			if (matcher.find()) {
				name = matcher.group(2);
				name = name.replace("_", " ").replaceFirst("^the ", "");
				if (isLowerCase) {
					name = CommonUtil.capitalize(name);
				}
			} else {
				name = oldbTeam.getTeamName();
				logger.warn(String.format("OpenLigaDB team named %s could not be translated into English "
						+ "via its IconUrl named %s.", oldbTeam.getTeamName(), oldbTeam.getTeamIconUrl()));
			}
			team.setName(name);
			//logger.info("TeamName: " + name + ", TeamIconUrl: " + oldbTeam.getTeamIconURL());

			String flag;
			if (teamMap.containsKey(name)) {
				flag = teamMap.get(name).getFlag();
			}
			else if (fifaCodeByCountryNameMap.containsKey(name)) {
				flag = fifaCodeByCountryNameMap.get(name);
				logger.warn(String.format("OpenLigaDB team named %s may have no flag %s image stored "
						+ "in local. Download %s image.", name, flag, oldbTeam.getTeamIconUrl()));
			}
			else {
				flag = "XYZ"; // unknown FIFA country code
				logger.warn(String.format("OpenLigaDB team named %s may have no flag image stored "
						+ "in local. Download %s image and update its %s dummy flag name at its team "
						+ "in the database.", name, flag, oldbTeam.getTeamIconUrl()));
			}
			team.setFlag(flag);
			
			team.setGroup(groupList.get(0)); // unfortunately real group is not stored here
			team.setFifaPoints((short) 0); // unknown
			team.setWsId(Long.valueOf(oldbTeam.getTeamId()));
			em.persist(team);
			teamMapByWsId.put(team.getWsId().intValue(), team);
		}
		
		List<com.msiggi.openligadb.model.Match> matchdatas = openLigaDBService.getMatchdata(LEAGUE_SHORTCUT, LEAGUE_SEASON);
		Collections.sort(matchdatas, (a, b) -> a.getMatchDateTime().compareTo(b.getMatchDateTime()));

		Map<Team, Integer> teamOccurenceMap = new HashMap<>();
		Map<String, List<Match>> matchesByRoundMap = new HashMap<>();
		for (int i=0; i < matchdatas.size(); i++) {
			com.msiggi.openligadb.model.Match matchdata = matchdatas.get(i);
			Match match = new Match();
			match.setEvent(event);
			match.setMatchN((short)(i+1));
			match.setTeam1(teamMapByWsId.get(matchdata.getTeam1().getTeamId()));
			if (match.getTeam1() != null) {
				teamOccurenceMap.put(match.getTeam1(), teamOccurenceMap.get(match.getTeam1()) == null ? 1 : teamOccurenceMap.get(match.getTeam1())+1);
			}
			match.setTeam2(teamMapByWsId.get(matchdata.getTeam2().getTeamId()));
			if (match.getTeam2() != null) {
				teamOccurenceMap.put(match.getTeam2(), teamOccurenceMap.get(match.getTeam2()) == null ? 1 : teamOccurenceMap.get(match.getTeam2())+1);
			}
			
			match.setStartTime(matchdata.getMatchDateTimeUTC());
			match.setRound(roundMap.get(matchdata.getGroup().getGroupID()));

			if (matchdata.getGroup().getGroupName().equals("1. Runde")) {
				String groupName = String.valueOf(matchdata.getLocation().getLocationCity());
				if (groupName != null) {
					Group group = groupList.stream().filter(e -> e.getName().equals(groupName)).findFirst().get();
					match.getTeam1().setGroup(group);
					match.getTeam2().setGroup(group);
					em.flush();
				}
			}
			else if (matchdata.getGroup().getGroupName().matches("^\\d\\. Runde$")) { // 2nd and 3rd rounds
			}
			else if (matchdata.getGroup().getGroupName().equals("Achtelfinale")) {
				// Sieger Gruppe B vs Dritter Gruppe A/C/D -> B1-ACD3
				String participantsRule = null;
				Matcher matcher = Pattern.compile("("+String.join("|", ORDNUNGZAHLEN)+")( Gruppe )(.*)").matcher(matchdata.getTeam1().getTeamName());
				if (matcher.find()) {
					participantsRule = matcher.group(3).replace("/", "")+(ORDNUNGZAHLEN.indexOf(matcher.group(1))+1); 
				}
				/*Matcher*/ matcher = Pattern.compile("("+String.join("|", ORDNUNGZAHLEN)+")( Gruppe )(.*)").matcher(matchdata.getTeam2().getTeamName());
				if (matcher.find()) {
					participantsRule += "-" + matcher.group(3).replace("/", "")+(ORDNUNGZAHLEN.indexOf(matcher.group(1))+1); 
				}
				match.setParticipantsRule(participantsRule);
				//logger.info("participantsRule: "+participantsRule);
				
				if (matchesByRoundMap.get("Achtelfinale") == null) {
					matchesByRoundMap.put("Achtelfinale", new ArrayList<>());
				}
				matchesByRoundMap.get("Achtelfinale").add(match);
			}
			else if (matchdata.getGroup().getGroupName().contains("Viertelfinale")) {
				// Sieger Achtelfinale 1 vs	Sieger Achtelfinale 2
				String participantsRule = null;
				Matcher matcher = Pattern.compile("(Sieger Achtelfinale )(\\d+)").matcher(matchdata.getTeam1().getTeamName());
				if (matcher.find()) {
					participantsRule = "W"+matchesByRoundMap.get("Achtelfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
				}
				/*Matcher*/ matcher = Pattern.compile("(Sieger Achtelfinale )(\\d+)").matcher(matchdata.getTeam2().getTeamName());
				if (matcher.find()) {
					participantsRule += "-W"+matchesByRoundMap.get("Achtelfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
				}
				match.setParticipantsRule(participantsRule);
				//logger.info("participantsRule: "+participantsRule);
				
				if (matchesByRoundMap.get("Viertelfinale") == null) {
					matchesByRoundMap.put("Viertelfinale", new ArrayList<Match>());
				}
				matchesByRoundMap.get("Viertelfinale").add(match);
			}
			else if (matchdata.getGroup().getGroupName().contains("Halbfinale")) {
				// Sieger Viertelfinale 1 vs Sieger Viertelfinale 2
				String participantsRule = null;
				Matcher matcher = Pattern.compile("(Sieger Viertelfinale )(\\d+)").matcher(matchdata.getTeam1().getTeamName());
				if (matcher.find()) {
					participantsRule = "W"+matchesByRoundMap.get("Viertelfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
				}
				/*Matcher*/ matcher = Pattern.compile("(Sieger Viertelfinale )(\\d+)").matcher(matchdata.getTeam2().getTeamName());
				if (matcher.find()) {
					participantsRule += "-W"+matchesByRoundMap.get("Viertelfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
				}
				match.setParticipantsRule(participantsRule);
				//logger.info("participantsRule: "+participantsRule);

				if (matchesByRoundMap.get("Halbfinale") == null) {
					matchesByRoundMap.put("Halbfinale", new ArrayList<Match>());
				}
				matchesByRoundMap.get("Halbfinale").add(match);
			}
			else if (matchdata.getGroup().getGroupName().contains("Spiel um Platz 3")) {
				// Verlierer Halbfinale 1 vs Verlierer Halbfinale 2
				String participantsRule = null;
				Matcher matcher = Pattern.compile("(Verlierer Halbfinale )(\\d+)").matcher(matchdata.getTeam1().getTeamName());
				if (matcher.find()) {
					participantsRule = "L"+matchesByRoundMap.get("Halbfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
				}
				/*Matcher*/ matcher = Pattern.compile("(Verlierer Halbfinale )(\\d+)").matcher(matchdata.getTeam2().getTeamName());
				if (matcher.find()) {
					participantsRule += "-L"+matchesByRoundMap.get("Halbfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
				}
				match.setParticipantsRule(participantsRule);
				//logger.info("participantsRule: "+participantsRule);
			}
			else if (matchdata.getGroup().getGroupName().contains("Finale")) {
				// Sieger Halbfinale 1 vs Sieger Halbfinale 2
				String participantsRule = null;
				Matcher matcher = Pattern.compile("(Sieger Halbfinale )(\\d+)").matcher(matchdata.getTeam1().getTeamName());
				if (matcher.find()) {
					participantsRule = "W"+matchesByRoundMap.get("Halbfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
				}
				/*Matcher*/ matcher = Pattern.compile("(Sieger Halbfinale )(\\d+)").matcher(matchdata.getTeam2().getTeamName());
				if (matcher.find()) {
					participantsRule += "-W"+matchesByRoundMap.get("Halbfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
				}
				match.setParticipantsRule(participantsRule);
				//logger.info("participantsRule: "+participantsRule);
			}
			else {
				String msg = String.format("Unsupported OpenLigaDB \"%s\" group name found of a match. "
						+ "Update the import script.", matchdata.getGroup().getGroupName());
				logger.error(msg);
				throw new OpenLigaDBException(msg);
			}
			
			em.persist(match);
		}
		
		WebService webService = new WebService();
		webService.setEvent(event);
		webService.setPriority((byte)1);
		webService.setLeagueShortcut(LEAGUE_SHORTCUT);
		webService.setLeagueSaison(LEAGUE_SEASON);
		webService.setResultNormalLabel("nach Nachspielzeit");
		webService.setResultExtraLabel("nach Verlängerung");
		webService.setResultPenaltyLabel("nach Elfmeterschießen");
		em.persist(webService);

		if (params.containsKey("TestMode") && (boolean)params.get("TestMode")) {
			logger.warn("Because TestMode is on, changes are not commited to the database.");
			return false; // changes are not to be committed in test mode
		}
		
		return true;
	}
	
}
