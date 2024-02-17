package com.zematix.jworldcup.backend.service;

import java.time.LocalDateTime;

import javax.inject.Inject;

import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.model.PublishedEvent;

/**
 * Asynchron called methods around {@link Match} elements.
 */
@Service
@Transactional
public class MatchAsyncService extends ServiceBase {

	@Inject
	private MatchService matchService;

	/**
	 * Invoked from
	 * {@link MatchService#saveMatch(Long, boolean, Boolean, LocalDateTime, Byte, Byte, Byte, Byte, Byte, Byte)
	 * when a match result is saved.
	 * 
	 * @param event - contains the saved match
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Async
	@TransactionalEventListener
	public void onUpdateMatchEvent(@NonNull PublishedEvent<Match> event) throws ServiceException {
		Match match = event.getEntity();
		logger.info("onUpdateMatchEvent matchId: {}", match.getMatchId());

		matchService.onUpdateMatchEvent(match);
	}
}
