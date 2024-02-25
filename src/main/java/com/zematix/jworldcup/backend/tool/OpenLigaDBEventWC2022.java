package com.zematix.jworldcup.backend.tool;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.msiggi.openligadb.model.League;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Group;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.Round;
import com.zematix.jworldcup.backend.entity.Team;
import com.zematix.jworldcup.backend.exception.OpenLigaDBException;

/**
 * Imports WC2022 event from OpenLigaDB. It was not difficult to implement it, there
 * were just minor issues.
 */
public class OpenLigaDBEventWC2022 extends OpenLigaDBEvent {
	
	/**
	 * Imports event belongs to {@link OpenLigaDBEventWC2022}
	 * 
	 * @return {@code true} if the modifications are commitable, {@code false} otherwise
	 * @throws OpenLigaDBException 
	 */
	@Override
	public boolean importEvent() throws OpenLigaDBException {
		EntityManager em = (EntityManager) params.get("EntityManager");
		checkNotNull(em, "Parameter named EntityManager is not set, its value cannot be null.");
		
		final String LEAGUE_SHORTCUT = "wmk";
		final String LEAGUE_SEASON = "2022";

		List<League> oldbLeagues = openLigaDBService.getAvailableLeagues();
		oldbLeagues.stream()
				.filter(e -> LEAGUE_SEASON.equals(e.getLeagueSeason()))
				.forEach(e -> logger.info(String.format("%s %s %s %s", e.getLeagueId(), e.getLeagueName(), e.getLeagueSeason(), e.getLeagueShortcut())));
		League league =
				oldbLeagues.stream().filter(
						e -> LEAGUE_SHORTCUT.equals(e.getLeagueShortcut()) && LEAGUE_SEASON.equals(e.getLeagueSeason()))
						.findFirst().orElse(null);
		if (league == null) {
			String msg = String.format("League is not found in OpenLigaDB where "
					+ "leagueShortcut=%s and leagueSaison=%s.",
					LEAGUE_SHORTCUT, LEAGUE_SEASON);
			logger.error(msg);
			throw new OpenLigaDBException(msg);
		}
		// 4537, Fußball-WM 2022 (Katar), 2022, wmk
		
		Event event = new Event();
//		event.setLocation(league.getLeagueName());
		event.setLocation("Qatar");
		event.setYear(Short.valueOf(league.getLeagueSeason()));
//		event.setDescription(league.getLeagueName());
		event.setDescription("World Cup");
		event.setShortDesc("WC");
		event.setOrganizer("FIFA");
		em.persist(event);
		
		logger.info("eventId={}", event.getEventId());
		
		List<com.msiggi.openligadb.model.Group> oldbGroups = openLigaDBService.getAvailableGroups(LEAGUE_SHORTCUT, LEAGUE_SEASON);
//		39225, Vorrunde Spieltag 1, 1
//		39226, Vorrunde Spieltag 2, 2
//		39227, Vorrunde Spieltag 3, 3
//		39228, Achtelfinale, 4
//		39229, Viertelfinale, 5
//		39230, Halbfinale, 6
//		39277, Spiel um Platz 3, 7
//		39278, Finale, 8

		Map<Integer, Round> roundMap = new HashMap<>();
		List<Round> roundList = new ArrayList<>();
		for (com.msiggi.openligadb.model.Group oldbGroup : oldbGroups) {
			Round round = new Round();
			round.setEvent(event);
			// Vorrunde Spieltag #, Achtelfinale, Viertelfinale, Halbfinale, Spiel um Platz 3, Finale
			String name = oldbGroup.getGroupName();
			switch (name) {
				case "Vorrunde Spieltag 1":
					name = "1st round";
					break;
				case "Vorrunde Spieltag 2":
					name = "2nd round";
					break;
				case "Vorrunde Spieltag 3":
					name = "3rd round";
					break;
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
				default:
					String msg = String.format("Unsupported OpenLigaDB \"%s\" group name found. "
							+ "Update the import script.", name);
					logger.error(msg);
					throw new OpenLigaDBException(msg);
			}
			round.setName(name);
			round.setIsGroupmatchAsBoolean(oldbGroup.getGroupName().contains("Vorrunde Spieltag"));
			em.persist(round);
			roundMap.put(oldbGroup.getGroupID(), round);
			roundList.add(round);
		}
		
		TypedQuery<Team> query = em.createNamedQuery("Team.findAll", Team.class);
		List<Team> teams = query.getResultList();
		
		List<com.msiggi.openligadb.model.Team> olTeams = openLigaDBService.getAvailableTeams(LEAGUE_SHORTCUT, LEAGUE_SEASON);
		
		List<Group> groupList = new ArrayList<>();
		olTeams.stream().filter(e -> e.getTeamGroupName() != null).sorted((e1, e2) -> {return e1.getTeamGroupName().compareTo(e2.getTeamGroupName());}).forEach(olTeam -> {
			if (groupList.stream().noneMatch(e -> e.getName().equals(olTeam.getTeamGroupName()))) {
				Group group = new Group();
				group.setEvent(event);
				group.setName(olTeam.getTeamGroupName());
				logger.info("Group: {}", group.getName());
				em.persist(group);
				groupList.add(group);
			}
		});

		Map<Integer, Team> teamMapByWsId = new HashMap<>();
		olTeams.stream().filter(e -> e.getTeamGroupName() != null).forEach(olTeam -> {
			Map<String, String> flagMap = Map.of("COS","CRC", "IRA", "IRN", "JAP", "JPN", "KAT", "QAT", "SAR", "KSA"); // JWorldcup uses FIFA country codes
			Team team = new Team();
			team.setEvent(event);
			Team oTeam = teams.stream().filter(e -> e.getFlag().equals(flagMap.getOrDefault(olTeam.getShortName(),olTeam.getShortName()))).findFirst().orElse(null);
			String name = oTeam != null ? oTeam.getName() : "XYZ";
			String flag = oTeam != null ? oTeam.getFlag() : "XYZ";
			if (olTeam.getShortName().equals("CAN")) { // Canada is missing yet from JWorldcup DB. Welcome!
				name = "Canada";
				flag = "CAN";
			}
			team.setName(name);
			team.setFlag(flag);
			team.setGroup(groupList.stream().filter(e -> e.getName().equals(olTeam.getTeamGroupName())).findFirst().orElse(null));
			team.setFifaPoints((short) 0); // unknown
			team.setWsId(Long.valueOf(olTeam.getTeamId()));
			logger.info("TeamName: {}, TeamName auf Deutsch: {}, Flag: {}, WsId: {}",  name, olTeam.getTeamName(), flag, olTeam.getTeamId());
			em.persist(team);
			teamMapByWsId.put(team.getWsId().intValue(), team);
		});
		
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
			if (matchdata.getGroup().getGroupName().contains("Vorrunde Spieltag")) { // Vorrunde Spieltag #
				match.setRound(roundList.get(teamOccurenceMap.get(match.getTeam1())-1));
			}
			else if (matchdata.getGroup().getGroupName().equals("Achtelfinale")) {
				match.setRound(roundList.get(3));
				
				// Sieger Gr. A vs Zweiter Gr. B A1-B2 
				String participantsRule = null;
				Matcher matcher = Pattern.compile("(Sieger|Zweiter)( Gr\\. )(.*)").matcher(matchdata.getTeam1().getTeamName());
				if (matcher.find()) {
					participantsRule = matcher.group(3)+("Sieger".equals(matcher.group(1)) ? "1" : "2"); 
				}
				/*Matcher*/ matcher = Pattern.compile("(Sieger|Zweiter)( Gr\\. )(.*)").matcher(matchdata.getTeam2().getTeamName());
				if (matcher.find()) {
					participantsRule += "-"+matcher.group(3)+("Sieger".equals(matcher.group(1)) ? "1" : "2"); 
				}
				match.setParticipantsRule(participantsRule);
				logger.info("participantsRule: {}", participantsRule);
				
				if (matchesByRoundMap.get("Achtelfinale") == null) {
					matchesByRoundMap.put("Achtelfinale", new ArrayList<>());
				}
				matchesByRoundMap.get("Achtelfinale").add(match);
			}
			else if (matchdata.getGroup().getGroupName().contains("Viertelfinale")) {
				match.setRound(roundList.get(4));

				// Sieger AF5 vs Sieger AF6
				String participantsRule = null;
				Matcher matcher = Pattern.compile("(Sieger AF)(\\d+)").matcher(matchdata.getTeam1().getTeamName());
				if (matcher.find()) {
					participantsRule = "W"+matchesByRoundMap.get("Achtelfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
				}
				/*Matcher*/ matcher = Pattern.compile("(Sieger AF)(\\d+)").matcher(matchdata.getTeam2().getTeamName());
				if (matcher.find()) {
					participantsRule += "-W"+matchesByRoundMap.get("Achtelfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
				}
				match.setParticipantsRule(participantsRule);
				logger.info("participantsRule: {}", participantsRule);
				
				if (matchesByRoundMap.get("Viertelfinale") == null) {
					matchesByRoundMap.put("Viertelfinale", new ArrayList<Match>());
				}
				matchesByRoundMap.get("Viertelfinale").add(match);
			}
			else if (matchdata.getGroup().getGroupName().contains("Halbfinale")) {
				match.setRound(roundList.get(5));
				
				// Sieger VF2 vs Sieger VF1 
				String participantsRule = null;
				Matcher matcher = Pattern.compile("(Sieger VF)(\\d+)").matcher(matchdata.getTeam1().getTeamName());
				if (matcher.find()) {
					participantsRule = "W"+matchesByRoundMap.get("Viertelfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
				}
				/*Matcher*/ matcher = Pattern.compile("(Sieger VF)(\\d+)").matcher(matchdata.getTeam2().getTeamName());
				if (matcher.find()) {
					participantsRule += "-W"+matchesByRoundMap.get("Viertelfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
				}
				match.setParticipantsRule(participantsRule);
				logger.info("participantsRule: {}", participantsRule);

				if (matchesByRoundMap.get("Halbfinale") == null) {
					matchesByRoundMap.put("Halbfinale", new ArrayList<Match>());
				}
				matchesByRoundMap.get("Halbfinale").add(match);
			}
			else if (matchdata.getGroup().getGroupName().contains("Spiel um Platz 3")) {
				match.setRound(roundList.get(6));
				
				// Verlierer HF1 vs Verlierer HF2
				String participantsRule = null;
				Matcher matcher = Pattern.compile("(Verlierer HF)(\\d+)").matcher(matchdata.getTeam1().getTeamName());
				if (matcher.find()) {
					participantsRule = "L"+matchesByRoundMap.get("Halbfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
				}
				/*Matcher*/ matcher = Pattern.compile("(Verlierer HF)(\\d+)").matcher(matchdata.getTeam2().getTeamName());
				if (matcher.find()) {
					participantsRule += "-L"+matchesByRoundMap.get("Halbfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
				}
				match.setParticipantsRule(participantsRule);
				logger.info("participantsRule: {}", participantsRule);
			}
			else if (matchdata.getGroup().getGroupName().contains("Finale")) {
				match.setRound(roundList.get(7));
				
				// Sieger HF1 vs Sieger HF2 
				String participantsRule = null;
				Matcher matcher = Pattern.compile("(Sieger HF)(\\d+)").matcher(matchdata.getTeam1().getTeamName());
				if (matcher.find()) {
					participantsRule = "W"+matchesByRoundMap.get("Halbfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
				}
				/*Matcher*/ matcher = Pattern.compile("(Sieger HF)(\\d+)").matcher(matchdata.getTeam2().getTeamName());
				if (matcher.find()) {
					participantsRule += "-W"+matchesByRoundMap.get("Halbfinale").get(Integer.valueOf(matcher.group(2))-1).getMatchN(); 
				}
				match.setParticipantsRule(participantsRule);
				logger.info("participantsRule: {}", participantsRule);
			}
			em.persist(match);
		}

		if (params.containsKey("TestMode") && (boolean)params.get("TestMode")) {
			logger.warn("Because TestMode is on, changes are not commited to the database.");
			return false; // changes are not to be committed in test mode
		}
		
		return true;
	}
}
