package com.zematix.jworldcup.backend.tool;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

/**
 * @deprecated
 * Imports WC2018 event from OpenLigaDB. Retrieving the groups was hard because
 * either they are not stored or although there are stored but on a tricky way, . 
 * namely groups are stored among the teams. Having run this script on June of 2018,
 * there was need some post-database work. The project has been modified a lot 
 * afterwards.Do not try to run it again without revise the code. 
 */
@Deprecated
public class OpenLigaDBEventWC2018 extends OpenLigaDBEvent {

	public OpenLigaDBEventWC2018() {
	}

	/**
	 * Imports event belongs to {@link OpenLigaDBEventWC2018}
	 * 
	 * @return {@code true} if the modifications are commitable, {@code false} otherwise
	 */
	@Override
	public boolean importEvent() {
		EntityManager em = (EntityManager) params.get("EntityManager");
		checkNotNull(em, "Parameter named EntityManager is not set, its value cannot be null.");
		
		//final String LEAGUE_SHORTCUT = "wmrussland"; // IS_GROUP_STORED false
		final String LEAGUE_SHORTCUT = "wm2018ru"; // IS_GROUP_STORED true
		final String LEAGUE_SAISON = "2018";
		final boolean IS_GROUP_STORED = true;

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

		com.msiggi.openligadb.client.League league = 
		oldbLeagues
				.stream()
				.filter(e -> LEAGUE_SHORTCUT.equals(e.getLeagueShortcut()) && LEAGUE_SAISON.equals(e.getLeagueSaison()))
				.findFirst().get();

		// 4220, Fußball-Weltmeisterschaft 2018 - Russland, 2018, wmrussland
		// 4215, Weltmeisterschaft 2018 - Russland, 2018, wm2018ru
		
		Event event = new Event();
		event.setLocation(league.getLeagueName());
		event.setYear(Short.valueOf(league.getLeagueSaison()));
		event.setDescription(league.getLeagueName());
		event.setShortDesc("WC");
		event.setOrganizer("FIFA");
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
		if (IS_GROUP_STORED) {
			Round round = new Round();
			round.setEvent(event);
			round.setName("1st round");
			round.setIsGroupmatchAsBoolean(true);
			em.persist(round);
			roundList.add(round);

			/*Round*/ round = new Round();
			round.setEvent(event);
			round.setName("2nd round");
			round.setIsGroupmatchAsBoolean(true);
			em.persist(round);
			roundList.add(round);
		
			/*Round*/ round = new Round();
			round.setEvent(event);
			round.setName("3rd round");
			round.setIsGroupmatchAsBoolean(true);
			em.persist(round);
			roundList.add(round);
		}
		for (com.msiggi.openligadb.client.Group oldbGroup : oldbGroups) {
			if (IS_GROUP_STORED) {
				if (oldbGroup.getGroupName().contains("Gruppe")) {
					continue;
				}
			}
			Round round = new Round();
			round.setEvent(event);
			// Achtelfinale, Viertelfinale, Halbfinale, Spiel um Platz 3, Finale
			String name = oldbGroup.getGroupName();
			switch (name) {
				case "Achtelfinale":
					name = "Round of 16";
					break;
				case "Viertelfinale":
					name = "Quarter-finals";
					break;
				case "Halbfinale":
					name = "Semi-finals";
					break;
				case "Spiel um Platz 3":
					name = "Third place play-off";
					break;
				case "Finale":
					name = "Final";
					break;
			}
			round.setName(name);
			round.setIsGroupmatchAsBoolean(oldbGroup.getGroupName().contains("Spieltag"));
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
		if (!IS_GROUP_STORED) {
			for (int i=0; i < oldbTeams.size()/4; i++) {
				Group group = new Group();
				group.setEvent(event);
				group.setName(String.valueOf((char)('A'+i)));
				logger.info("Group: " + group.getName());
				em.persist(group);
				groupList.add(group);
			}
		}
		else {
			for (com.msiggi.openligadb.client.Group oldbGroup : oldbGroups) {
				if (oldbGroup.getGroupName().contains("Gruppe")) {
					Group group = new Group();
					group.setEvent(event);
					group.setName(String.valueOf(oldbGroup.getGroupName().charAt(oldbGroup.getGroupName().length()-1)));
					logger.info("Group: " + group.getName());
					em.persist(group);
					groupList.add(group);				}
			}
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
			// url fortunately contains english name
			Matcher matcher = Pattern.compile("(.*?_of_)(.*?)(_%28.*|\\.svg.*)").matcher(oldbTeam.getTeamIconURL());
			String name = matcher.find() ? matcher.group(2).replace("_", " ") : oldbTeam.getTeamName();
			team.setName(name);
			logger.info("TeamName: " + name + ", Teamurl: " + oldbTeam.getTeamIconURL());
			team.setFlag(teamMap.containsKey(name) ? teamMap.get(name).getFlag() : "XYZ");
			team.setGroup(groupList.get(0)); // unfortunately real group is not stored
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
			if (IS_GROUP_STORED) {
				Collections.sort(matchdatas, (a, b) -> a.getMatchDateTime().compare(b.getMatchDateTime()));
			}
			
		}
		catch (/*WebService*/Exception e) {
			//throw new OpenLigaDBException(e.getMessage());
		}

		Map<Team, Integer> teamOccurenceMap = new HashMap<>();
		Map<String, List<Match>> matchesByRoundMap = new HashMap<>();
		for (int i=0; i < matchdatas.size(); i++) {
			Matchdata matchdata = matchdatas.get(i);
			Match match = new Match();
			match.setEvent(event);
			match.setMatchN((short)(i+1));
			match.setTeam1(teamMapByWsId.get(matchdata.getIdTeam1()));
			if (match.getTeam1() != null) {
				teamOccurenceMap.put(match.getTeam1(), teamOccurenceMap.get(match.getTeam1()) == null ? 1 : teamOccurenceMap.get(match.getTeam1())+1);
			}
			match.setTeam2(teamMapByWsId.get(matchdata.getIdTeam2()));
			if (match.getTeam2() != null) {
				teamOccurenceMap.put(match.getTeam2(), teamOccurenceMap.get(match.getTeam2()) == null ? 1 : teamOccurenceMap.get(match.getTeam2())+1);
			}
			match.setStartTime(new Timestamp(matchdata.getMatchDateTime().toGregorianCalendar().getTimeInMillis()).toLocalDateTime());
			if (!IS_GROUP_STORED) {
				match.setRound(roundMap.get(matchdata.getGroupID()));
			}
			else {
				if (matchdata.getGroupName().contains("Gruppe")) {
					String groupName = String.valueOf(matchdata.getGroupName().charAt(matchdata.getGroupName().length()-1));
					Group group = groupList.stream().filter(e -> e.getName().equals(groupName)).findFirst().get();
					match.getTeam1().setGroup(group);
					em.persist(match.getTeam1());
					match.getTeam2().setGroup(group);
					em.persist(match.getTeam2());
					
					match.setRound(roundList.get(teamOccurenceMap.get(match.getTeam1())-1));
				}
				else if (matchdata.getGroupName().equals("Achtelfinale")) {
					match.setRound(roundList.get(3));
					
					// 1. Gruppe C vs 2. Gruppe D -> C1-D2
					String participantsRule = null;
					Matcher matcher = Pattern.compile("(\\d+)(\\. Gruppe )(.*)").matcher(matchdata.getNameTeam1());
					if (matcher.find()) {
						participantsRule = matcher.group(3)+matcher.group(1); 
					}
					/*Matcher*/ matcher = Pattern.compile("(\\d+)(\\. Gruppe )(.*)").matcher(matchdata.getNameTeam2());
					if (matcher.find()) {
						participantsRule += "-"+matcher.group(3)+matcher.group(1); 
					}
					match.setParticipantsRule(participantsRule);
					logger.info("participantsRule: "+participantsRule);
					
					if (matchesByRoundMap.get("Achtelfinale") == null) {
						matchesByRoundMap.put("Achtelfinale", new ArrayList<Match>());
					}
					matchesByRoundMap.get("Achtelfinale").add(match);
				}
				else if (matchdata.getGroupName().contains("Viertelfinale")) {
					match.setRound(roundList.get(4));

					// Sieger Achtelfinale 1 vs	Sieger Achtelfinale 2
					String participantsRule = null;
					Matcher matcher = Pattern.compile("(Sieger Achtelfinale )(\\d+)").matcher(matchdata.getNameTeam1());
					if (matcher.find()) {
						participantsRule = "W"+matchesByRoundMap.get("Achtelfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
					}
					/*Matcher*/ matcher = Pattern.compile("(Sieger Achtelfinale )(\\d+)").matcher(matchdata.getNameTeam2());
					if (matcher.find()) {
						participantsRule += "-W"+matchesByRoundMap.get("Achtelfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
					}
					match.setParticipantsRule(participantsRule);
					logger.info("participantsRule: "+participantsRule);
					
					if (matchesByRoundMap.get("Viertelfinale") == null) {
						matchesByRoundMap.put("Viertelfinale", new ArrayList<Match>());
					}
					matchesByRoundMap.get("Viertelfinale").add(match);
				}
				else if (matchdata.getGroupName().contains("Halbfinale")) {
					match.setRound(roundList.get(5));
					
					// Sieger Viertelfinale 1 vs Sieger Viertelfinale 2
					String participantsRule = null;
					Matcher matcher = Pattern.compile("(Sieger Viertelfinale )(\\d+)").matcher(matchdata.getNameTeam1());
					if (matcher.find()) {
						participantsRule = "W"+matchesByRoundMap.get("Viertelfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
					}
					/*Matcher*/ matcher = Pattern.compile("(Sieger Viertelfinale )(\\d+)").matcher(matchdata.getNameTeam2());
					if (matcher.find()) {
						participantsRule += "-W"+matchesByRoundMap.get("Viertelfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
					}
					match.setParticipantsRule(participantsRule);
					logger.info("participantsRule: "+participantsRule);

					if (matchesByRoundMap.get("Halbfinale") == null) {
						matchesByRoundMap.put("Halbfinale", new ArrayList<Match>());
					}
					matchesByRoundMap.get("Halbfinale").add(match);
				}
				else if (matchdata.getGroupName().contains("Spiel um Platz 3")) {
					match.setRound(roundList.get(6));
					
					// Verlierer Halbfinale 1 vs Verlierer Halbfinale 2
					String participantsRule = null;
					Matcher matcher = Pattern.compile("(Verlierer Halbfinale )(\\d+)").matcher(matchdata.getNameTeam1());
					if (matcher.find()) {
						participantsRule = "L"+matchesByRoundMap.get("Halbfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
					}
					/*Matcher*/ matcher = Pattern.compile("(Verlierer Halbfinale )(\\d+)").matcher(matchdata.getNameTeam2());
					if (matcher.find()) {
						participantsRule += "-L"+matchesByRoundMap.get("Halbfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
					}
					match.setParticipantsRule(participantsRule);
					logger.info("participantsRule: "+participantsRule);
				}
				else if (matchdata.getGroupName().contains("Finale")) {
					match.setRound(roundList.get(7));
					
					// Sieger Halbfinale 1 vs Sieger Halbfinale 2
					String participantsRule = null;
					Matcher matcher = Pattern.compile("(Sieger Halbfinale )(\\d+)").matcher(matchdata.getNameTeam1());
					if (matcher.find()) {
						participantsRule = "W"+matchesByRoundMap.get("Halbfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
					}
					/*Matcher*/ matcher = Pattern.compile("(Sieger Halbfinale )(\\d+)").matcher(matchdata.getNameTeam2());
					if (matcher.find()) {
						participantsRule += "-W"+matchesByRoundMap.get("Halbfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
					}
					match.setParticipantsRule(participantsRule);
					logger.info("participantsRule: "+participantsRule);
				}
			}
			em.persist(match);
		}
		
		return true;
	}
}
