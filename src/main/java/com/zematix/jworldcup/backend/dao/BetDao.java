package com.zematix.jworldcup.backend.dao;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;
import com.zematix.jworldcup.backend.entity.Bet;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.QBet;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserGroup;

/**
 * Database operations around {@link Bet} entities.
 */
@Component
@Transactional
public class BetDao extends DaoBase {

//	@Inject
//	private CommonDao commonDao;

	/**
	 * Returns a list of all {@link Bet} entities from database.
	 * 
	 * @return list of all {@link Bet} entities
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Bet> getAllBets() {
		TypedQuery<Bet> query = getEntityManager().createNamedQuery("Bet.findAll", Bet.class);
		return query.getResultList();
	}
	
	/**
	 * Returns found {@link Bet} instance with the provided {@code matchId}
	 * and {@code userId}.
	 * 
	 * @param matchId
	 * @param userId
	 * @return found {@link Bet} instance
	 * @throws IllegalArgumentException if any of the given parameters is null
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Bet findBetByMatchAndUser(Long matchId, Long userId) {
		Bet bet = null;
		
		QBet qBet = QBet.bet;
		JPAQuery<Bet> query = new JPAQuery<>(getEntityManager());
		bet = query.from(qBet)
				.where(qBet.match.matchId.eq(matchId),
						qBet.user.userId.eq(userId))
				.fetchOne();

		return bet;
	}

	/**
	 * Returns a list of found {@link Bet} instances with the provided {@code eventId}
	 * and {@code userId}. It is ordered by {@link Bet#getMatch()#getStartTime()}.
	 * 
	 * @param eventId
	 * @param userId
	 * @return list of found {@Bet} instances
	 * @throws IllegalArgumentException if any of the given parameters is {@code null}
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Bet> retrieveBetsByEventAndUser(Long eventId, Long userId) {
		List<Bet> bets = null;
		checkNotNull(eventId);
		checkNotNull(userId);
		
		QBet qBet = QBet.bet;
		JPAQuery<Bet> query = new JPAQuery<>(getEntityManager());
		bets = query.from(qBet)
				.where(qBet.event.eventId.eq(eventId),
						qBet.user.userId.eq(userId))
				.orderBy(qBet.match.startTime.asc())
				.fetch();

		return bets;
	}

	/**
	 * Returns {@link Bet} instances belongs to the provided {@link Match#matchId} and {@link UserGroup#userGroupId}.
	 * @param matchId
	 * @param userGroupId
	 * @return found bets
	 */
	@Transactional(readOnly = true)
	public List<Bet>  retrieveBetsByMatchAndUserGroup(Long matchId, Long userGroupId) {
		List<Bet> bets = null;
		checkNotNull(matchId);
		checkNotNull(userGroupId);
		
		QBet qBet = QBet.bet;
		JPAQuery<Bet> query = new JPAQuery<>(getEntityManager());
		bets = query.from(qBet)
				.where(qBet.match.matchId.eq(matchId),
						qBet.user.userGroups.any().userGroupId.eq(userGroupId))
				.orderBy(qBet.user.loginName.asc())
				.fetch();

		return bets;		
	}
	
	/**
	 * Delete all bets of the given user.
	 * 
	 * @param userId - belongs to an {@link User} entity
	 * @throws IllegalArgumentException if any of the given parameters is {@code null}
	 */
	public void deleteBetsByUser(Long userId) {
		checkNotNull(userId);
		
		QBet qBet = QBet.bet;
		new JPADeleteClause(getEntityManager(), qBet)
				.where(qBet.user.userId.eq(userId)).execute();
	}
}
