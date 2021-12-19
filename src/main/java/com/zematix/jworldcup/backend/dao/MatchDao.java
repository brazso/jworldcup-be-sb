package com.zematix.jworldcup.backend.dao;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQuery;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.QMatch;
import com.zematix.jworldcup.backend.entity.Team;

/**
 * Database operations around {@link Match} entities.
 */
@Component
@Transactional
public class MatchDao extends DaoBase {

	/**
	 * Returns a list of all {@link Match} entities from database.
	 * 
	 * @return list of all {@link Match} entities
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Match> getAllMatches() {
		TypedQuery<Match> query = getEntityManager().createNamedQuery("Match.findAll", Match.class);
		List<Match> matches = query.getResultList();
		return matches;
	}
	
	/**
	 * Returns a list of {@link Match} instances belongs to the given {@code eventId} parameter.
	 * The result list is ordered by {@link Match#startTime}.
	 *
	 * @param eventId
	 * @return list of {@link Match} instances belongs to the {@link Event} of the given {@code eventId}
	 * @throws IllegalArgumentException if any of the arguments is null
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Match> retrieveMatchesByEvent(Long eventId) {
		List<Match> matches;
		
		QMatch qMatch = QMatch.match;
		JPAQuery<Match> query = new JPAQuery<>(getEntityManager());
		matches = query.from(qMatch)
			.where(qMatch.event.eventId.eq(eventId))
			.orderBy(qMatch.startTime.asc())
			.fetch();
		
		return matches;
	}
	
	/**
	 * Returns a list of {@link Match} instances where each match element has the given 
	 * {@code teamId} participant, the match is finished (has valid result) and 
	 * the match is played in the group stage.
	 *
	 * @param teamId
	 * @return list of {@link Match} instances containing all finished matches belongs to the given team
	 * @throws IllegalArgumentException if any of the parameters is null
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Match> retrieveFinishedGroupMatchesByTeam(Long teamId) {
		List<Match> matches;
		
		QMatch qMatch = QMatch.match;
		JPAQuery<Match> query = new JPAQuery<>(getEntityManager());
		matches = query.from(qMatch)
			.where((qMatch.team1.teamId.eq(teamId).or(qMatch.team2.teamId.eq(teamId)))
					.and(qMatch.goalNormalByTeam1.isNotNull().and(qMatch.goalNormalByTeam2.isNotNull()))
					.and(qMatch.round.isGroupmatch.eq((byte) 1)))
			.fetch();
		
		return matches;
	}
	
	/**
	 * Returns a list of {@link Match} instances where each match element belongs to the 
	 * group specified by the given {@code groupId}, the match is finished (has valid result) 
	 * and the match is played in the group stage.
	 * 
	 * @param groupId
	 * @return list of {@link Match} instances belongs to the given {@code groupId}
	 * @throws IllegalArgumentException if any of the parameters is null
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Match> retrieveFinishedGroupMatchesByGroup(Long groupId) {
		List<Match> matches;
		
		QMatch qMatch = QMatch.match;
		JPAQuery<Match> query = new JPAQuery<>(getEntityManager());
		matches = query.from(qMatch)
			.where((qMatch.team1.group.groupId.eq(groupId).or(qMatch.team2.group.groupId.eq(groupId)))
					.and(qMatch.goalNormalByTeam1.isNotNull().and(qMatch.goalNormalByTeam2.isNotNull()))
					.and(qMatch.round.isGroupmatch.eq((byte) 1)))
			.fetch();
		
		return matches;
	}
	
	/**
	 * Returns a list of {@link Match#participantsRule} values of all matches
	 * belongs to the provided {@code eventId} event. Only the non empty
	 * values are retrieved of the knock-out matches where there is at least
	 * a missing team participant.
	 * 
	 * @param eventId
	 * @return list of participant rules of all matches of the provided event
	 * @throws IllegalArgumentException if any of the parameters is null
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<String> retrieveParticipantRulesOfMatchesByEvent(Long eventId) {
		List<Match> matches;
		
		QMatch qMatch = QMatch.match;
		JPAQuery<Match> query = new JPAQuery<>(getEntityManager());
		matches = query.from(qMatch)
			.where(qMatch.event.eventId.eq(eventId)
					.and(qMatch.round.isGroupmatch.eq((byte) 0))
					.and(qMatch.participantsRule.isNotEmpty())
					.and((qMatch.team1.teamId.isNull().or(qMatch.team2.teamId.isNull()))))
			.orderBy(qMatch.matchN.asc())
			.fetch();
		
		return matches.stream().map(match->match.getParticipantsRule()).toList();
	}

	/**
	 * Retrieves {@link Match} instance belongs to the given {@code eventId} and
	 *  {@code matchN}.
	 * 
	 * @param eventId
	 * @param matchN - number of the match started from 1
	 * @return found {@Match} instance or {@code null} if not found
	 * @throws IllegalArgumentException if any of the parameters is null
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Match retrieveMatchByMatchN(Long eventId, Short matchN) {
		Match match = null;
		
		QMatch qMatch = QMatch.match;
		JPAQuery<Match> query = new JPAQuery<>(getEntityManager());
		match = query.from(qMatch)
			.where(qMatch.event.eventId.eq(eventId)
					.and(qMatch.matchN.eq(matchN)))
			.fetchFirst();
		
		return match;
	}

	/**
	 * Retrieves a list of {@link Match} instances belongs to the  given 
	 * {@code eventId}, with not null {@link Match#participantsRule} value, located in the 
	 * knockout stage and has at least one {@link Team} participant with {@code null} value.
	 * 
	 * @param eventId
	 * @return list of {@link Match} instances where each element is in the knockout stage with 
	 *         participant rule and with missing participant team(s)
	 * @throws IllegalArgumentException if any of the parameters is null 
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Match> retrieveMatchesWithoutParticipantsByEvent(Long eventId) {
		List<Match> matches;
		
		QMatch qMatch = QMatch.match;
		JPAQuery<Match> query = new JPAQuery<>(getEntityManager());
		matches = query.from(qMatch)
			.where(qMatch.event.eventId.eq(eventId)
					.and(qMatch.round.isGroupmatch.eq((byte) 0))
					.and(qMatch.participantsRule.isNotEmpty())
					.and((qMatch.team1.teamId.isNull().or(qMatch.team2.teamId.isNull()))))
			.fetch();
		
		return matches;
	}

	/**
	 * Returns a list of {@link Match} instances where each match element belongs to the 
	 * group specified by the given {@code groupId} and the match is played in the group stage.
	 * 
	 * @param groupId
	 * @return list of {@link Match} instances belongs to the given {@code groupId}
	 * @throws IllegalArgumentException if any of the parameters is null
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Match> retrieveMatchesByGroup(Long groupId) {
		List<Match> matches;
		
		QMatch qMatch = QMatch.match;
		JPAQuery<Match> query = new JPAQuery<>(getEntityManager());
		matches = query.from(qMatch)
			.where((qMatch.team1.group.groupId.eq(groupId).or(qMatch.team2.group.groupId.eq(groupId)))
					.and(qMatch.round.isGroupmatch.eq((byte) 1)))
			.fetch();
		
		return matches;
	}
}
