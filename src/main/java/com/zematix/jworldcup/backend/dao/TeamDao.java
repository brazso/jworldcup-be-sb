package com.zematix.jworldcup.backend.dao;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQuery;
import com.zematix.jworldcup.backend.entity.QMatch;
import com.zematix.jworldcup.backend.entity.QTeam;
import com.zematix.jworldcup.backend.entity.Team;

/**
 * Database operations around {@link Teams} entities.
 */
@Component
@Transactional
public class TeamDao extends DaoBase {
	
	/**
	 * @return list of all {@link Team} entities from database
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Team> getAllTeams() {
		TypedQuery<Team> query = getEntityManager().createNamedQuery("Team.findAll", Team.class);
		List<Team> teams = query.getResultList();
		return teams;
	}

	/**
	 * Retrieves all {@link Team} instances belongs to the given {@code eventId}. The result is
	 * sorted by the name of its elements. Among the returned teams user can select 
	 * later a favourite one.
	 * 
	 * @param eventId
	 * @return
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Team> retrieveFavouriteGroupTeams(Long eventId) {
		List<Team> teams;
		
		QTeam qTeam = QTeam.team;
		JPAQuery<Team> query = new JPAQuery<>(getEntityManager());
		teams = query.from(qTeam)
			.where(qTeam.event.eventId.eq(eventId))
			.orderBy(qTeam.name.asc())
			.fetch();
		
		return teams;
	}
	
	/**
	 * Retrieves all teams of the knockout phase belongs to the given {@code eventId}. 
	 * The result is sorted by the name of its elements. Among the returned teams user can 
	 * select later a favourite one.
	 * 
	 * @param eventId
	 * @return
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Team> retrieveFavouriteKnockoutTeams(Long eventId) {
		List<Team> teams;
		
		QTeam qTeam = QTeam.team;
		QMatch qMatch = QMatch.match;
		JPAQuery<Team> query = new JPAQuery<>(getEntityManager());
		teams = query.from(qTeam, qMatch)
			.where(qTeam.event.eventId.eq(eventId)
					.and((qMatch.team1.eq(qTeam).or(qMatch.team2.eq(qTeam))))
					.and(qMatch.round.isGroupmatch.eq((byte)0)))
			.distinct()
			.orderBy(qTeam.name.asc())
			.fetch();

		return teams;
	}

	/**
	 * Retrieves a team of an event belongs to the given 
	 * {@code Event#eventId} and with the given {@link Team#wsId}.
	 * 
	 * @param eventId
	 * @param wsId - webService id of the team
	 * @return a team given by the parameters, {@code null} if not found
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Team retrieveTeamByWsId(Long eventId, Long wsId) {
		Team team = null;
		
		QTeam qTeam = QTeam.team;
		JPAQuery<Team> query = new JPAQuery<>(getEntityManager());
		team = query.from(qTeam)
			.where(qTeam.event.eventId.eq(eventId)
					.and(qTeam.wsId.eq(wsId)))
			.fetchFirst();

		return team;
	}

}
