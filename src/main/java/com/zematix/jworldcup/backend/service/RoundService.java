package com.zematix.jworldcup.backend.service;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.dao.RoundDao;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Round;

/**
 * Operations around {@link Round} elements. 
 * Usually it injects at least its DAO class belongs to the same entities. 
 * It may also inject other service and DAO classes.
 */
@Service
@Transactional
public class RoundService extends ServiceBase {
	
	@Inject
	private RoundDao roundDao;
	
	/**
	 * @return list of all Role entities from database
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Round> getAllRounds() {
		return roundDao.getAllRounds();
	}

	/**
	 * Returns a list of {@link Round} instances belongs to the given {@code eventId} parameter.
	 * The result list is ordered by {@link Round#roundId}.
	 * 
	 * @param eventId
	 * @return list of {@link Round} instances belongs to the given {@link Event#eventId} parameter
	 * @throws IllegalArgumentException if any of the arguments is null 
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Round> retrieveRoundsByEvent(Long eventId) {
		return roundDao.retrieveRoundsByEvent(eventId);
	}
}
