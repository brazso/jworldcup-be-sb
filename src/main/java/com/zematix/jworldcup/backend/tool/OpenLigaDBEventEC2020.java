package com.zematix.jworldcup.backend.tool;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.msiggi.openligadb.client.ArrayOfGroup;
import com.msiggi.openligadb.client.ArrayOfLeague;
import com.msiggi.openligadb.client.ArrayOfMatchdata;
import com.msiggi.openligadb.client.ArrayOfTeam;
import com.msiggi.openligadb.client.Matchdata;
import com.msiggi.openligadb.client.Sportsdata;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Group;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.Round;
import com.zematix.jworldcup.backend.entity.Team;
import com.zematix.jworldcup.backend.entity.WebService;
import com.zematix.jworldcup.backend.util.CommonUtil;

/**
 * Imports EC2020 event from OpenLigaDB. Retrieving the groups (from A to F) was impossibe
 * because it cannot be retrieved from OpenLigaDB.
 * Having run this script on 2021-06-06, there were lots of post-database work. 
 * The project has been modified a lot later. Do not try to run it again without revise the code.
 * 
 * Problems after execution of this import:
 * - OpenLigaDB team named Finland may have no flag FIN image stored in local. Download https://upload.wikimedia.org/wikipedia/commons/thumb/b/bc/Flag_of_Finland.svg/200px-Flag_of_Finland.svg.png image.
 * - OpenLigaDB team named North Macedonia may have no flag MKD image stored in local. Download https://upload.wikimedia.org/wikipedia/commons/thumb/7/79/Flag_of_North_Macedonia.svg/640px-Flag_of_North_Macedonia.svg.png image.
 * - OpenLigaDB team named Scotland may have no flag SCO image stored in local. Download https://upload.wikimedia.org/wikipedia/commons/thumb/1/10/Flag_of_Scotland.svg/1024px-Flag_of_Scotland.svg.png image.
 */
public class OpenLigaDBEventEC2020 extends OpenLigaDBEvent {

	/**
	 * Imports event belongs to {@link OpenLigaDBEventEC2020}
	 * 
	 * @return {@code true} if the modifications are commitable, {@code false} otherwise
	 */
	@Override
	public boolean importEvent() {
		EntityManager em = (EntityManager) params.get("EntityManager");
		checkNotNull(em, "Parameter named EntityManager is not set, its value cannot be null.");
		
		final String EVENT_LOCATION = "England and others";
		final String EVENT_DESCRIPTION = "Euro Cup";
		final String EVENT_SHORT_DESC = "EC";
		final Short EVENT_YEAR = 2020;
		final String EVENT_ORGANIZER = "UEFA";
		
		final String LEAGUE_SHORTCUT = "em20"; // IS_GROUP_STORED false
		final String LEAGUE_SAISON = "2020";
		
		final int TEAMS_IN_GROUP = 4; // number of teams in a group

		Map<String, String> fifaCodeByCountryNameMap = retrieveFifaCodeByCountryNameMap();
		checkNotNull(fifaCodeByCountryNameMap, "Retrieved fifaCodeByCountryNameMap cannot be null.");

		List<com.msiggi.openligadb.client.League> oldbLeagues = new ArrayList<>();
		Sportsdata sportsdata = null;
		try {
			/*Sportsdata*/ sportsdata = new Sportsdata();
			ArrayOfLeague aol = sportsdata.getSportsdataSoap12().getAvailLeaguesBySports(/*sportID*/ 1);
			/*List<Sport>*/ oldbLeagues = aol.getLeague();
		}
		catch (/*WebService*/Exception e) {
			//throw new OpenLigaDBException(e.getMessage());
		}

		oldbLeagues.stream()
				.filter(e -> LEAGUE_SAISON.equals(e.getLeagueSaison()))
				.forEach(e -> logger.info(String.format("%s %s %s %s", e.getLeagueID(), e.getLeagueName(), e.getLeagueSaison(), e.getLeagueShortcut())));

		// 4220, Fußball-Weltmeisterschaft 2018 - Russland, 2018, wmrussland
		// 4215, Weltmeisterschaft 2018 - Russland, 2018, wm2018ru
		
		Event event = new Event();
		event.setLocation(EVENT_LOCATION);
		event.setYear(EVENT_YEAR);
		event.setDescription(EVENT_DESCRIPTION);
		event.setShortDesc(EVENT_SHORT_DESC);
		event.setOrganizer(EVENT_ORGANIZER);
		em.persist(event);
		
		logger.info("eventId="+event.getEventId());
		
		List<com.msiggi.openligadb.client.Group> oldbGroups = new ArrayList<>();
		//Sportsdata sportsdata = null;
		try {
			//Sportsdata sportsdata = new Sportsdata();
			ArrayOfGroup aog = sportsdata.getSportsdataSoap12().getAvailGroups(LEAGUE_SHORTCUT, LEAGUE_SAISON);
			/*List<Sport>*/ oldbGroups = aog.getGroup();
		}
		catch (/*WebService*/Exception e) {
			//throw new OpenLigaDBException(e.getMessage());
		}

//		30803, 1. Spieltag, 1
//		30804, 2. Spieltag, 2
//		30805, 3. Spieltag, 3
//		30806, Achtelfinale, 4
//		30841, Viertelfinale, 5
//		30842, Halbfinale, 6
//		30843, Finale, 7

		Map<Integer, Round> roundMap = new HashMap<>();
		List<Round> roundList = new ArrayList<>();
		for (com.msiggi.openligadb.client.Group oldbGroup : oldbGroups) {
			boolean isGroupMatch = false;
			Round round = new Round();
			round.setEvent(event);
			// Achtelfinale, Viertelfinale, Halbfinale, Spiel um Platz 3, Finale
			String name = oldbGroup.getGroupName();
			switch (name) {
				case "1. Runde Gruppenphase":
					name = "1st round";
					isGroupMatch = true;
					break;
				case "2. Runde Gruppenphase":
					name = "2nd round";
					isGroupMatch = true;
					break;
				case "3. Runde Gruppenphase":
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
				case "Finale":
					name = "Final";
					isGroupMatch = false;
					break;
				default:
					break;
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

		List<com.msiggi.openligadb.client.Team> oldbTeams = new ArrayList<>();
		//Sportsdata sportsdata = null;
		try {
			//Sportsdata sportsdata = new Sportsdata();
			ArrayOfTeam aot = sportsdata.getSportsdataSoap12().getTeamsByLeagueSaison(LEAGUE_SHORTCUT, LEAGUE_SAISON);
			/*List<Sport>*/ oldbTeams = aot.getTeam();
		}
		catch (/*WebService*/Exception e) {
			//throw new OpenLigaDBException(e.getMessage());
		}
		
		List<Group> groupList = new ArrayList<>();
		for (int i=0; i < oldbTeams.size() / TEAMS_IN_GROUP; i++) {
			Group group = new Group();
			group.setEvent(event);
			group.setName(String.valueOf((char)('A'+i)));
			logger.info("Group: " + group.getName());
			em.persist(group);
			groupList.add(group);
		}
		
		Map<Integer, Team> teamMapByWsId = new HashMap<>();
		for (com.msiggi.openligadb.client.Team oldbTeam : oldbTeams) {
			if (oldbTeam.getTeamName().contains("Gruppe")
					|| oldbTeam.getTeamName().contains("Sieger")
					|| oldbTeam.getTeamName().contains("Verlierer")) {
				continue;
			}
					
			Team team = new Team();
			team.setEvent(event);
			boolean isLowerCase = false;
			// url #1 (mostly used) contains English name, it can be used for translation
			// e.g. https://upload.wikimedia.org/wikipedia/commons/thumb/5/53/Flag_of_Syria.svg/20px-Flag_of_Syria.svg.png
			Matcher matcher = Pattern.compile("(.*?_of_)(.*?)(_%28.*|\\.svg.*)").matcher(oldbTeam.getTeamIconURL());
			if (!matcher.find()) {
				// url #2 (rarely used) contains English name, it can be used for translation
				// e.g. http://www.nationalflaggen.de/media/flags/flagge-thailand.gif
				matcher = Pattern.compile("(.*?flagge-)(.*?)(\\..*)").matcher(oldbTeam.getTeamIconURL());
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
						+ "via its IconUrl named %s.", oldbTeam.getTeamName(), oldbTeam.getTeamIconURL()));
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
						+ "in local. Download %s image.", name, flag, oldbTeam.getTeamIconURL()));
			}
			else {
				flag = "XYZ"; // unknown FIFA country code
				logger.warn(String.format("OpenLigaDB team named %s may have no flag image stored "
						+ "in local. Download %s image and update its %s dummy flag name at its team "
						+ "in the database.", name, flag, oldbTeam.getTeamIconURL()));
			}
			team.setFlag(flag);
			
			team.setGroup(groupList.get(0)); // unfortunately real group cannot be retrieved form OpenLigaDB
			String groupName = retrieveTeamGroupDictionary().get(name);
			Group group = groupList.stream().filter(e -> e.getName().equals(groupName)).findFirst().orElse(null);
			if (group != null) {
				team.setGroup(group);
			}
			team.setFifaPoints((short) 0); // unknown
			team.setWsId(Long.valueOf(oldbTeam.getTeamID()));
			em.persist(team);
			teamMapByWsId.put(team.getWsId().intValue(), team);
		}
		
		List<Matchdata> matchdatas = new ArrayList<>();
		try {
			//Sportsdata sportsdata = new Sportsdata();
			ArrayOfMatchdata aomd = sportsdata.getSportsdataSoap12().getMatchdataByLeagueSaison(LEAGUE_SHORTCUT, LEAGUE_SAISON);
			/*List<Matchdata>*/ matchdatas = aomd.getMatchdata();
		}
		catch (/*WebService*/Exception e) {
			//throw new OpenLigaDBException(e.getMessage());
		}

		for (int i=0; i < matchdatas.size(); i++) {
			Matchdata matchdata = matchdatas.get(i);
			Match match = new Match();
			match.setEvent(event);
			match.setMatchN((short)(i+1));
			match.setTeam1(teamMapByWsId.get(matchdata.getIdTeam1()));
			match.setTeam2(teamMapByWsId.get(matchdata.getIdTeam2()));
			match.setStartTime(new Timestamp(matchdata.getMatchDateTime().toGregorianCalendar().getTimeInMillis()).toLocalDateTime());
			match.setRound(roundMap.get(matchdata.getGroupID()));

			em.persist(match);
		}
		
		WebService webService = new WebService();
		webService.setEvent(event);
		webService.setPriority((byte)1);
		webService.setLeagueShortcut(LEAGUE_SHORTCUT);
		webService.setLeagueSaison(LEAGUE_SAISON);
		webService.setResultNormalLabel("Ergebnis90");
		webService.setResultExtraLabel("Ergebnis120");
		webService.setResultPenaltyLabel("Ergebnis11");
		em.persist(webService);

		addMissingMatches(event, roundList.stream().filter(e -> Boolean.FALSE.equals(e.getIsGroupmatchAsBoolean())).collect(Collectors.toList()),
				matchdatas.size(), teamMapByWsId);
		
		if (params.containsKey("TestMode") && (boolean)params.get("TestMode")) {
			logger.warn("Because TestMode is on, changes are not commited to the database.");
			return false; // changes are not to be committed in test mode
		}
		
		return true;

	}
	
	private void addMissingMatches(Event event, List<Round> roundList, int matchesSize, Map<Integer, Team> teamMapByWsId) {
		EntityManager em = (EntityManager) params.get("EntityManager");
		checkNotNull(em, "Parameter named EntityManager is not set, its value cannot be null.");
		final DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		
		for (Round round : roundList) {
			switch (round.getName()) {
				case "Round of 16": {
					Match match = new Match();
					match.setEvent(event);
					match.setMatchN((short)(++matchesSize));
					match.setStartTime(LocalDateTime.parse("2021-06-26 21:00", sdf));
					match.setRound(round);
					match.setParticipantsRule("A1-C2");
					em.persist(match);
					
					/*Match*/ match = new Match();
					match.setEvent(event);
					match.setMatchN((short)(++matchesSize));
					match.setStartTime(LocalDateTime.parse("2021-06-26 18:00", sdf));
					match.setRound(round);
					match.setParticipantsRule("A2-B2");
					em.persist(match);
					
					/*Match*/ match = new Match();
					match.setEvent(event);
					match.setMatchN((short)(++matchesSize));
					match.setStartTime(LocalDateTime.parse("2021-06-27 21:00", sdf));
					match.setRound(round);
					match.setParticipantsRule("B1-ADEF3");
					em.persist(match);
					
					/*Match*/ match = new Match();
					match.setEvent(event);
					match.setMatchN((short)(++matchesSize));
					match.setStartTime(LocalDateTime.parse("2021-06-27 18:00", sdf));
					match.setRound(round);
					match.setParticipantsRule("C1-DEF3");
					em.persist(match);
					
					/*Match*/ match = new Match();
					match.setEvent(event);
					match.setMatchN((short)(++matchesSize));
					match.setStartTime(LocalDateTime.parse("2021-06-28 21:00", sdf));
					match.setRound(round);
					match.setParticipantsRule("F1-ABC3");
					em.persist(match);
					
					/*Match*/ match = new Match();
					match.setEvent(event);
					match.setMatchN((short)(++matchesSize));
					match.setStartTime(LocalDateTime.parse("2021-06-28 18:00", sdf));
					match.setRound(round);
					match.setParticipantsRule("D2-E2");
					em.persist(match);
					
					/*Match*/ match = new Match();
					match.setEvent(event);
					match.setMatchN((short)(++matchesSize));
					match.setStartTime(LocalDateTime.parse("2021-06-29 21:00", sdf));
					match.setRound(round);
					match.setParticipantsRule("E1-ABCD3");
					em.persist(match);
					
					/*Match*/ match = new Match();
					match.setEvent(event);
					match.setMatchN((short)(++matchesSize));
					match.setStartTime(LocalDateTime.parse("2021-06-29 18:00", sdf));
					match.setRound(round);
					match.setParticipantsRule("D12-F2");
					em.persist(match);
					break;
				}
				case "Quarter-finals": {
					Match match = new Match();
					match.setEvent(event);
					match.setMatchN((short)(++matchesSize));
					match.setStartTime(LocalDateTime.parse("2021-07-02 18:00", sdf));
					match.setRound(round);
					match.setParticipantsRule("W41-W42");
					em.persist(match);
					
					/*Match*/ match = new Match();
					match.setEvent(event);
					match.setMatchN((short)(++matchesSize));
					match.setStartTime(LocalDateTime.parse("2021-07-02 21:00", sdf));
					match.setRound(round);
					match.setParticipantsRule("W39-W37");
					em.persist(match);
					
					/*Match*/ match = new Match();
					match.setEvent(event);
					match.setMatchN((short)(++matchesSize));
					match.setStartTime(LocalDateTime.parse("2021-07-03 18:00", sdf));
					match.setRound(round);
					match.setParticipantsRule("W40-W38");
					em.persist(match);
					
					/*Match*/ match = new Match();
					match.setEvent(event);
					match.setMatchN((short)(++matchesSize));
					match.setStartTime(LocalDateTime.parse("2021-07-03 21:00", sdf));
					match.setRound(round);
					match.setParticipantsRule("W43-W44");
					em.persist(match);
					break;
				}
				case "Semi-finals": {
					Match match = new Match();
					match.setEvent(event);
					match.setMatchN((short)(++matchesSize));
					match.setStartTime(LocalDateTime.parse("2021-07-06 21:00", sdf));
					match.setRound(round);
					match.setParticipantsRule("W46-W45");
					em.persist(match);
					
					/*Match*/ match = new Match();
					match.setEvent(event);
					match.setMatchN((short)(++matchesSize));
					match.setStartTime(LocalDateTime.parse("2021-07-07 21:00", sdf));
					match.setRound(round);
					match.setParticipantsRule("W48-W47");
					em.persist(match);
					break;
				}
				case "Final": {
					Match match = new Match();
					match.setEvent(event);
					match.setMatchN((short)(++matchesSize));
					match.setStartTime(LocalDateTime.parse("2021-07-11 21:00", sdf));
					match.setRound(round);
					match.setParticipantsRule("W49-W50");
					em.persist(match);
					break;
				}
				default:
					break;
			}
			
		}
	}
	
	/**
	 * Returns a German-English dictionary for missing team names.
	 * @return
	 */
	private Map<String, String> retrieveTeamGroupDictionary() {
		return Stream.of(new String[][] {
			{ "Turkey", "A" }, 
			{ "Italy", "A" },
			{ "Wales", "A" },
			{ "Switzerland", "A" },
			{ "Denmark", "B" },
			{ "Finland", "B" },
			{ "Belgium", "B" },
			{ "Russia", "B" },
			{ "Netherlands", "C" },
			{ "Ukraine", "C" },
			{ "Austria", "C" },
			{ "North Macedonia", "C" },
			{ "England", "D" },
			{ "Croatia", "D" },
			{ "Scotland", "D" },
			{ "Czech Republic", "D" },
			{ "Spain", "E" },
			{ "Sweden", "E" },
			{ "Poland", "E" },
			{ "Slovakia", "E" },
			{ "Hungary", "F" },
			{ "Portugal", "F" },
			{ "France", "F" },
			{ "Germany", "F" }
			}).collect(Collectors.toMap(data -> data[0], data -> data[1]));
	}

}
