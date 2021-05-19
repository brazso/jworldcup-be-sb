package com.zematix.jworldcup.backend.repository;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.dao.BetDao;
import com.zematix.jworldcup.backend.entity.Bet;
import com.zematix.jworldcup.backend.entity.User;

/**
 * A weak attempt to migrate {@link BetDao} to repository. Default interface methods
 * contain the original methods and they calls the real interface ones. However
 * the repository caller cannot caught the possible original exceptions, only their wrapper, 
 * a {@link InvalidDataAccessApiUsageException}. Finally it is not used in the app, 
 * querydsl looks generally better than the spring repositories.  
 */
@Transactional
@Repository
public interface BetRepository extends JpaRepository<Bet, Long> {

	/**
	 * Returns a list of all {@link Bet} entities from database.
	 * 
	 * @return list of all {@link Bet} entities
	 */
	default List<Bet> getAllBets() {
		return this.findAll();
	}
	
	Bet findByMatchMatchIdAndUserUserId(Long matchId, Long userId);
	
	/**
	 * Returns found {@link Bet} instance with the provided {@code matchId}
	 * and {@code userId}.
	 * 
	 * @param matchId
	 * @param userId
	 * @return found {@link Bet} instance
	 * @throws IllegalArgumentException if any of the given parameters is null
	 */
	default Bet findBetByMatchAndUser(Long matchId, Long userId) {
		checkNotNull(matchId);
		checkNotNull(userId);
		return this.findByMatchMatchIdAndUserUserId(matchId, userId);
	}

	List<Bet> findByEventEventIdAndUserUserId(Long eventId, Long userId);

	/**
	 * Returns a list of found {@link Bet} instances with the provided {@code eventId}
	 * and {@code userId}. It is ordered by {@link Bet#getMatch()#getStartTime()}.
	 * 
	 * @param eventId
	 * @param userId
	 * @return list of found {@Bet} instances
	 * @throws IllegalArgumentException if any of the given parameters is {@code null}
	 */
	default List<Bet> retrieveBetsByEventAndUser(Long eventId, Long userId) {
		checkNotNull(eventId);
		checkNotNull(userId);
		return this.findByEventEventIdAndUserUserId(eventId, userId);
	}
	
	@Transactional
	void deleteByUserUserId(Long userId);
	
	/**
	 * Delete all bets of the given user.
	 * 
	 * @param userId - belongs to an {@link User} entity
	 * @throws IllegalArgumentException if any of the given parameters is {@code null}
	 */
	@Transactional
	default void deleteBetsByUser(Long userId) {
		checkNotNull(userId);
		this.deleteByUserUserId(userId);
	}
	
}
