package com.zematix.jworldcup.backend.tool;

import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

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
 * Imports EC2024 event from OpenLigaDB. Retrieving the groups (from A to F) was easy because somehow groups was
 * stored among the rounds in OpenLigaDB. However group-match rounds could be retrieve form match indexes. 
 * 
 * Having run this script on 2024-05-22, there were no post-database work. 
 * 
 * Problems after execution of this import: none
 */
public class OpenLigaDBEventEC2024 extends OpenLigaDBEvent {

	/**
	 * Imports event belongs to {@link OpenLigaDBEventEC2024}
	 * 
	 * @return {@code true} if the modifications are commitable, {@code false} otherwise
	 */
	@Override
	public boolean importEvent() throws OpenLigaDBException {
		EntityManager em = (EntityManager) params.get("EntityManager");
		checkNotNull(em, "Parameter named EntityManager is not set, its value cannot be null.");
		
		final String EVENT_LOCATION = "Germany";
		final String EVENT_DESCRIPTION = "Euro Cup";
		final String EVENT_SHORT_DESC = "EC";
		final Short EVENT_YEAR = 2024;
		final String EVENT_ORGANIZER = "UEFA";
		final String EVENT_WEBSITE = "https://en.wikipedia.org/wiki/UEFA_Euro_2024";
		
		final String LEAGUE_SHORTCUT = "em2024"; // IS_GROUP_STORED true
		final String LEAGUE_SEASON = "2024";
		
		final int TEAMS_IN_GROUP = 4; // number of teams in a group
		final List<String> ORDNUNGZAHLEN = Arrays.asList("Sieger", "Zweiter", "Dritter", "Vierter");

		Map<String, String> fifaCodeByCountryNameMap = retrieveFifaCodeByCountryNameMapFromCSV();

		List<League> oldbLeagues = openLigaDBService.getAvailableLeagues();
		oldbLeagues.stream()
				.filter(e -> LEAGUE_SEASON.equals(e.getLeagueSeason()))
				.forEach(e -> logger.info(String.format("%s, %s, %s, %s", e.getLeagueId(), e.getLeagueName(), e.getLeagueSeason(), e.getLeagueShortcut())));
		// 4694 Europameisterschaft 2024 2024 em2024
		// 4705 EURO 2024 (Herren) 2024 meuro2024
		// 4708 UEFA EURO 2024 2024 em24
		
		Event event = new Event();
		event.setLocation(EVENT_LOCATION);
		event.setYear(EVENT_YEAR);
		event.setDescription(EVENT_DESCRIPTION);
		event.setShortDesc(EVENT_SHORT_DESC);
		event.setOrganizer(EVENT_ORGANIZER);
		event.setWebsite(EVENT_WEBSITE);
		em.persist(event);
		
		List<com.msiggi.openligadb.model.Group> oldbGroups = openLigaDBService.getAvailableGroups(LEAGUE_SHORTCUT, LEAGUE_SEASON);
		oldbGroups.stream().forEach(e -> logger
				.info(String.format("%s, %s, %s", e.getGroupID(), e.getGroupName(), e.getGroupOrderID())));
		// 43756, Gruppe A, 1
		// 43757, Gruppe B, 2
		// 43758, Gruppe C, 3
		// 43759, Gruppe D, 4
		// 43760, Gruppe E, 5
		// 43761, Gruppe F, 6
		// 43762, Achtelfinale, 7
		// 43763, Viertelfinale, 8
		// 43764, Halbfinale, 9
		// 43765, Finale, 10

		Map<Integer, Round> roundMap = new HashMap<>();
		List<Round> roundList = new ArrayList<>();
		int totalGroups = 0;
		for (com.msiggi.openligadb.model.Group oldbGroup : oldbGroups) {
			boolean isGroupMatch = false;
			Round round = new Round();
			round.setEvent(event);
			// Gruppe [A-F], Achtelfinale, Viertelfinale, Halbfinale, Finale
			String name = oldbGroup.getGroupName();
			switch (name) {
				case "Gruppe A":
					name = "1st round";
					isGroupMatch = true;
					totalGroups++;
					break;
				case "Gruppe C":
					name = "2nd round";
					isGroupMatch = true;
					totalGroups++;
					break;
				case "Gruppe E":
					name = "3rd round";
					isGroupMatch = true;
					totalGroups++;
					break;
				case "Gruppe B":
				case "Gruppe D":
				case "Gruppe F":
					totalGroups++;
					continue;
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
				case "Finale":
					name = "Final";
					isGroupMatch = false;
					break;
				default:
					break;
			}
			round.setName(name);
			round.setIsGroupmatchAsBoolean(isGroupMatch);
			if (!isGroupMatch) {
				round.setIsOvertimeAsBoolean(true);
			}
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
					.collect(Collectors.toMap(Team::getName, t -> t));

		List<com.msiggi.openligadb.model.Team> oldbTeams = openLigaDBService.getAvailableTeams(LEAGUE_SHORTCUT, LEAGUE_SEASON);
		List<com.msiggi.openligadb.model.Team> oldbRealTeams = oldbTeams.stream().filter(e -> !(e.getTeamName().contains("Gruppe")
				|| e.getTeamName().contains("Sieger")
				|| e.getTeamName().contains("Verlierer"))).toList();
		
		List<Group> groupList = new ArrayList<>();
		for (int i=0; i < oldbRealTeams.size() / TEAMS_IN_GROUP; i++) {
			Group group = new Group();
			group.setEvent(event);
			group.setName(String.valueOf((char)('A'+i)));
			logger.info("Group: {}", group.getName());
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
			} else if (retrieveTeamDictionary().containsKey(oldbTeam.getTeamName())) {
				name = retrieveTeamDictionary().get(oldbTeam.getTeamName());
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
			
			// real group can be retrieved via matches later and it cannot be null
			team.setGroup(groupList.get(0));

			team.setFifaPoints((short) 0); // unknown
			team.setWsId(Long.valueOf(oldbTeam.getTeamId()));
			em.persist(team);
			teamMapByWsId.put(team.getWsId().intValue(), team);
		}
		
		Map<String, List<Match>> matchesByRoundMap = new HashMap<>();
		List<com.msiggi.openligadb.model.Match> matchdatas = openLigaDBService.getMatchdata(LEAGUE_SHORTCUT, LEAGUE_SEASON);
		Collections.sort(matchdatas, (a, b) -> a.getMatchDateTime().compareTo(b.getMatchDateTime()));

		matchdatas.add(40, new com.msiggi.openligadb.model.Match()); // unfortunately there is a missing match from Achtelfinale
		for (int i=0; i < matchdatas.size(); i++) {
			if (i == 40) { // missing match
				Match match = new Match();
				match.setEvent(event);
				match.setMatchN((short)(i+1));
				final DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
				match.setStartTime(LocalDateTime.parse("2024-07-01 16:00", sdf)); // in UTC
				match.setRound(matchesByRoundMap.get("Achtelfinale").get(0).getRound()); // it's not the first one
				match.setParticipantsRule("D2-E2");
				matchesByRoundMap.get("Achtelfinale").add(match);
				em.persist(match);
				continue;
			}
			com.msiggi.openligadb.model.Match matchdata = matchdatas.get(i);
			Match match = new Match();
			match.setEvent(event);
			match.setMatchN((short)(i+1));
			match.setTeam1(teamMapByWsId.get(matchdata.getTeam1().getTeamId()));
			match.setTeam2(teamMapByWsId.get(matchdata.getTeam2().getTeamId()));
			match.setStartTime(matchdata.getMatchDateTimeUTC());
			final int totalGroupMatches = totalGroups * TEAMS_IN_GROUP*(TEAMS_IN_GROUP-1)/2;
			if (i < totalGroupMatches) {
				match.setRound(roundList.get(i/(totalGroupMatches/(int)roundList.stream().filter(e->e.getIsGroupmatchAsBoolean()).count())));
			}
			else { 
				match.setRound(roundMap.get(matchdata.getGroup().getGroupID()));				
			}

			// retrieve real group e.g. from "Gruppe B"
			Matcher matcher = Pattern.compile("^Gruppe (.*)$").matcher(matchdata.getGroup().getGroupName());
			if (matcher.find()) {
				String groupName = matcher.group(1); 

				Team team1 = teamMapByWsId.get(matchdata.getTeam1().getTeamId());
				em.refresh(team1);
				team1.setGroup(groupList.stream().filter(e -> groupName.equals(e.getName())).findFirst().orElse(null));
				Team team2 = teamMapByWsId.get(matchdata.getTeam2().getTeamId());
				em.refresh(team2);
				team2.setGroup(groupList.stream().filter(e -> groupName.equals(e.getName())).findFirst().orElse(null));
			}
			else if (matchdata.getGroup().getGroupName().equals("Achtelfinale")) {
				// Sieger Gruppe C vs Dritter Gruppe D/E/F -> C1-DEF3
				String participantsRule = null;
				matcher = Pattern.compile("("+String.join("|", ORDNUNGZAHLEN)+")( Gruppe )(.*)").matcher(matchdata.getTeam1().getTeamName());
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
			else if (matchdata.getGroup().getGroupName().equals("Viertelfinale")) {
				// Sieger Achtelfinale 1 vs Sieger Achtelfinale 2
				String participantsRule = null;
				matcher = Pattern.compile("(Sieger Achtelfinale )(\\d+)").matcher(matchdata.getTeam1().getTeamName());
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
					matchesByRoundMap.put("Viertelfinale", new ArrayList<>());
				}
				matchesByRoundMap.get("Viertelfinale").add(match);
			}
			else if (matchdata.getGroup().getGroupName().contains("Halbfinale")) {
				// Sieger Viertelfinale 1 vs Sieger Viertelfinale 2
				String participantsRule = null;
				matcher = Pattern.compile("(Sieger Viertelfinale )(\\d+)").matcher(matchdata.getTeam1().getTeamName());
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
					matchesByRoundMap.put("Halbfinale", new ArrayList<>());
				}
				matchesByRoundMap.get("Halbfinale").add(match);
			}
			else if (matchdata.getGroup().getGroupName().contains("Finale")) {
				// Sieger Halbfinale 1 vs Sieger Halbfinale 2
				String participantsRule = null;
				matcher = Pattern.compile("(Sieger Halbfinale )(\\d+)").matcher(matchdata.getTeam1().getTeamName());
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
		webService.setResultNormalLabel("Endergebnis");
		webService.setResultExtraLabel("nach Verlängerung");
		webService.setResultPenaltyLabel("nach Elfmeterscheißen");
		em.persist(webService);

		if (params.containsKey("TestMode") && (boolean)params.get("TestMode")) {
			logger.warn("Because TestMode is on, changes are not commited to the database.");
			return false; // changes are not to be committed in test mode
		}
		
		return true;
	}
}
