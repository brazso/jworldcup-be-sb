package com.zematix.jworldcup.backend.model;

import java.util.ArrayList;
import java.util.List;

import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.Team;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.service.MatchService;
import com.zematix.jworldcup.backend.service.ServerBase;

/**
 * Contains a {@link Team} and auxiliary data to calculate its position in the group.
 */
public class GroupTeam extends ServerBase {

	// "@Inject Logger logger" cannot be used here in non CDI environment
//	private static final Logger logger = LoggerFactory.getLogger(GroupTeam.class);

	//@Inject - cannot be used in non CDI environment
	private MatchService matchService;
	
	private Team team;
	
	/**
	 * Contains finished group matches of the actual team. 
	 */
	private List<Match> playedMatches = new ArrayList<>();
	
	/**
	 * Sometimes the calculations must be made among the played matches of some selected 
	 * teams instead of all teams belong to the group. Contains {@link Team#getTeamId()} 
	 * values. Empty filter means no filtering at all.
	 */
	private List<Long> filterTeamIds = new ArrayList<>();
	
	/**
	 * Position inside its group started from 1.
	 */
	private int positionInGroup = 1;
	
	private boolean isTeamInGroupFinished = false;

	/**
	 * Constructor to create a {@link GroupTeam} instance.
	 * @param team
	 * @param playedMatches - played group matches by team
	 */
	public GroupTeam(Team team, List<Match> playedMatches) {
		// without CDI (Inject) support the objects must be retrieved manually
		//this.matchService = CDI.current().select(MatchService.class).get();
		this.matchService = new MatchService(); // it is enough now, only an independent getMatchResult method is called from the class

		this.team = team;
		this.playedMatches = playedMatches;
		
		this.isTeamInGroupFinished = playedMatches != null && team.getGroup().getTeams().size()-1 == playedMatches.size();
	}

	/**
	 * @return the team
	 */
	public Team getTeam() {
		return team;
	}
	/**
	 * @param team the team to set
	 */
	public void setTeam(Team team) {
		this.team = team;
	}
	/**
	 * @return the playedMatches
	 */
	public List<Match> getPlayedMatches() {
		return playedMatches;
	}
	/**
	 * @param playedMatches the playedMatches to set
	 */
	public void setPlayedMatches(List<Match> playedMatches) {
		this.playedMatches = playedMatches;
	}
	/**
	 * @return the filterTeamIds
	 */
	public List<Long> getFilterTeamIds() {
		return filterTeamIds;
	}
	/**
	 * @param filterTeamIds the filterTeamIds to set
	 */
	public void setFilterTeamIds(List<Long> filterTeamIds) {
		this.filterTeamIds = filterTeamIds;
	}
	/**
	 * @return the positionInGroup
	 */
	public int getPositionInGroup() {
		return positionInGroup;
	}
	/**
	 * @param positionInGroup the positionInGroup to set
	 */
	public void setPositionInGroup(int positionInGroup) {
		this.positionInGroup = positionInGroup;
	}

	public boolean isTeamInGroupFinished() {
		return isTeamInGroupFinished;
	}

	public void setTeamInGroupFinished(boolean isTeamInGroupFinished) {
		this.isTeamInGroupFinished = isTeamInGroupFinished;
	}

	// calculated fields
	
	/**
	 * Calculates the score of the given {@code match} result from the given {@code} team side.
	 * 
	 * @param match
	 * @param teamId
	 * @return calculated score of the given {@code match}  from the given {@code} team side
	 */
	private int getMatchResult(Match match, Long teamId) {
		int matchResult = -2;
		
		if (!teamId.equals(match.getTeam1().getTeamId()) && !teamId.equals(match.getTeam2().getTeamId())) {
			return -2;
		}
		
		int side = teamId.equals(match.getTeam1().getTeamId()) ? 1 : 2;
		
		try {
			matchResult = matchService.getMatchResult(side, match.getGoalNormalByTeam1(), null, null, match.getGoalNormalByTeam2(), null, null);
		} catch (ServiceException e) {
			consumeServiceException(e);
		}
		return matchResult;
	}

	/**
	 * Returns true if the provided {@link Match} instance is inside 
	 * {@link GroupTeam#filterTeamIds}. If the latter list is empty, it
	 * always returns true.
	 * 
	 * @param match - {@link Match} instance to be checked
	 * @return {@code true} if {@code match} is inside {@link GroupTeam#filterTeamIds} 
	 * or the latter is empty 
	 */
	public boolean isInFilterTeams(Match match) {
		boolean result = false;
		if (this.filterTeamIds.isEmpty()) {
			result = true;
		} else if (this.filterTeamIds.contains(match.getTeam1().getTeamId())
				&& this.filterTeamIds.contains(match.getTeam2().getTeamId()))
			result = true;
		return result;
	}

	/**
	 * Returns the number of the played matches of the actual team filtered 
	 * by {@link GroupTeam#filterTeamIds}.
	 * 
	 * @return number of the played matches
	 */
	public int getMatchesPlayed() {
		int result = 0;
		for (Match match : this.playedMatches) {
			if (isInFilterTeams(match)) {
				result++;
			}
		}
		return result;
	}

	/**
	 * Returns the number of the winner matches of the actual team filtered 
	 * by {@link GroupTeam#filterTeamIds}.
	 * 
	 * @return number of the won played matches
	 */
	public int getWon() {
		int result = 0;
		for (Match match : this.playedMatches) {
			if (isInFilterTeams(match)) {
				result += (getMatchResult(match, this.team.getTeamId()) == 1 ? 1 : 0);
			}
		}
		return result;
	}

	/**
	 * Returns the number of the draw matches of the actual team filtered 
	 * by {@link GroupTeam#filterTeamIds}.
	 * 
	 * @return number of the draw played matches
	 */
	public int getDraw() {
		int result = 0;
		for (Match match : this.playedMatches) {
			if (isInFilterTeams(match)) {
				result += (getMatchResult(match, this.team.getTeamId()) == 0 ? 1 : 0);
			}
		}
		return result;
	}

	/**
	 * Returns the number of the lost matches of the actual team filtered 
	 * by {@link GroupTeam#filterTeamIds}.
	 * 
	 * @return number of the lost played matches
	 */
	public int getLost() {
		int result = 0;
		for (Match match : this.playedMatches) {
			if (isInFilterTeams(match)) {
				result += (getMatchResult(match, this.team.getTeamId()) == -1 ? 1 : 0);
			}
		}
		return result;
	}

	/**
	 * Returns the number of goals produced by the actual team filtered 
	 * by {@link GroupTeam#filterTeamIds}.
	 * 
	 * @return number of goals after played matches
	 */
	public int getGoalsFor() {
		int result = 0;
		for (Match match : this.playedMatches) {
			if (isInFilterTeams(match)) {
				int side = this.team.getTeamId().equals(match.getTeam1().getTeamId()) ? 1 : 2;
				result += (side==1 ? match.getGoalNormalByTeam1() : match.getGoalNormalByTeam2());
			}
		}
		return result;
	}

	/**
	 * Returns the number of goals received by the actual team filtered 
	 * by {@link GroupTeam#filterTeamIds}.
	 * 
	 * @return number of goals after played matches
	 */
	public int getGoalsAgainst() {
		int result = 0;
		for (Match match : this.playedMatches) {
			if (isInFilterTeams(match)) {
				int side = this.team.getTeamId().equals(match.getTeam1().getTeamId()) ? 1 : 2;
				result += (side==2 ? match.getGoalNormalByTeam1() : match.getGoalNormalByTeam2());
			}
		}
		return result;
	}

	/**
	 * Returns the total goal difference of actual team filtered 
	 * by {@link GroupTeam#filterTeamIds}.
	 * 
	 * @return total goal difference after played matches
	 */
	public int getGoalDifference() {
		return getGoalsFor() - getGoalsAgainst();
	}

	/**
	 * Returns the points produced by the actual team filtered 
	 * by {@link GroupTeam#filterTeamIds}.
	 * 
	 * @return points after played matches
	 */
	public int getPoints() {
		return getWon()*3 + getDraw();
	}
	
	/**
	 * Returns the short description concatenated with year of an event belongs 
	 * to the actual {@link GroupTeam} instance. For instance "WC2014".
	 * 
	 * @return short description concatenated with year of event
	 */
	public String getEventShortDescWithYear() {
		return this.team.getEvent().getShortDescWithYear();
	}
}
