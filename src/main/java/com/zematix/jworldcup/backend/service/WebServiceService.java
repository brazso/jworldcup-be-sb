package com.zematix.jworldcup.backend.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;
import com.msiggi.openligadb.model.MatchResult;
import com.zematix.jworldcup.backend.dao.WebServiceDao;
import com.zematix.jworldcup.backend.emun.ParameterizedMessageType;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.WebService;
import com.zematix.jworldcup.backend.exception.OpenLigaDBException;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.model.ParameterizedMessage;

/**
 * Operations around {@link WebService} elements. 
 * Usually it injects at least its DAO class belongs to the same entities. 
 * It may also inject other service and DAO classes.
 */
@Service
@Transactional
public class WebServiceService extends ServiceBase {

	@Inject 
	private WebServiceDao webServiceDao;
	
	@Inject
	private ApplicationService applicationService;

	@Inject
	private MatchService matchService;
	
	@Inject
	private OpenLigaDBService openLigaDBService;
	
	/**
	 * Locates the given {@code match} in the given {@code matchdata} list. 
	 * Returns the found matchdata otherwise {@code null}.
	 * 
	 * @param matchdatas - list where given match must be located
	 * @param match - searched object
	 * @return found match in matchdatas otherwise {@code null}
	 */
	@VisibleForTesting
	/*private*/ com.msiggi.openligadb.model.Match findMatchInMatchdatas(List<com.msiggi.openligadb.model.Match> matchdatas, Match match) {
		checkNotNull(matchdatas);
		checkNotNull(match);
		
		for (com.msiggi.openligadb.model.Match matchdata : matchdatas) {
			if (match.getStartTime().equals(matchdata.getMatchDateTimeUTC())) {
				if (match.getTeam1() == null || match.getTeam2() == null) {
					return matchdata;
				}
				else if ((match.getTeam1().getWsId().equals((long)matchdata.getTeam1().getTeamId())
					&& match.getTeam2().getWsId().equals((long)matchdata.getTeam2().getTeamId())) ||
						(match.getTeam1().getWsId().equals((long)matchdata.getTeam2().getTeamId())
								&& match.getTeam2().getWsId().equals((long)matchdata.getTeam1().getTeamId()))) {
					return matchdata;
				}
			}
		}
		return null;
	}

	/**
	 * Calls {@link WebServiceDao#retrieveWebServicesByEvent(Long)}. If there is no
	 * webService belongs to the given event, it throws ServiceException.
	 * 
	 * @param eventId - filter
	 * @throws ServiceException
	 * @return list of {@link WebService} objects belongs to the given eventId
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<WebService> retrieveWebServicesByEvent(Long eventId) throws ServiceException {
		checkNotNull(eventId);
		List<ParameterizedMessage> errMsgs = new ArrayList<>();
		
		List<WebService> webServices = webServiceDao.retrieveWebServicesByEvent(eventId);
		if (webServices.isEmpty()) {
			errMsgs.add(ParameterizedMessage.create("NO_ACTIVE_WEBSERVICE_FOR_EVENT", ParameterizedMessageType.WARNING, eventId));
			throw new ServiceException(errMsgs);
		}
		
		return webServices;
	}

	/**
	 * Updates incomplete but escalated matches from calling web service.
	 * Returns the updated matches.
	 * 
	 * @return list of updated matches
	 * @throws ServiceException
	 */
	public List<Match> updateMatchResults(Long eventId) throws ServiceException {
		List<Match> updatedMatches = new ArrayList<>();

		checkNotNull(eventId);

		List<ParameterizedMessage> errMsgs = new ArrayList<>();
		LocalDateTime actualDateTime = applicationService.getActualDateTime();
		
		List<WebService> webServices = retrieveWebServicesByEvent(eventId);
		for (WebService webService : webServices) {
			
			List<com.msiggi.openligadb.model.Match> matchdatas = new ArrayList<>();
			try {
				/*List<Matchdata>*/ matchdatas = openLigaDBService.getMatchdata(webService.getLeagueShortcut(), webService.getLeagueSaison());
			} catch (OpenLigaDBException e) {
				logger.error(e.getMessage(), e);
				errMsgs.add(ParameterizedMessage.create("WEBSERVICE_CALL_FAILED_FOR_METHOD", ParameterizedMessageType.ERROR, "getMatchdataByLeagueSaison"));
				throw new ServiceException(errMsgs);
			}
			
			List<Match> matches = matchService.retrieveIncompleteEscalatedMatchesByEvent(eventId, actualDateTime);
			for (Match match : matches) {
				com.msiggi.openligadb.model.Match matchdata = findMatchInMatchdatas(matchdatas, match);
				if (matchdata != null && matchdata.isMatchIsFinished()) {
					logger.info(String.format("Match with %d matchId is incomplete but escalated", match.getMatchId()));
					boolean isUpdated = updateMatchByMatchdata(match, matchdata, webService);
					if (isUpdated) {
						updatedMatches.add(match);
						logger.info(String.format("Escalated match with %d matchId is updated", match.getMatchId()));
					}
				}
			}
		}
		
		return updatedMatches;
	}

	/**
	 * Updates given match object with given matchdata object of the given webService.
	 * Results {@code true} if match was really updated with matchdata, its missing participant 
	 * teams or its final result were supplied.
	 * 
	 * @param match - match to be updated
	 * @param matchdata - webService's matchdata belongs to the match
	 * @param webService
	 * @return {@code true} if there was real database update
	 * @throws ServiceException
	 */
	@VisibleForTesting
	/*private*/ boolean updateMatchByMatchdata(Match match, com.msiggi.openligadb.model.Match matchdata, WebService webService) throws ServiceException {
		checkNotNull(match);
		checkNotNull(matchdata);
		checkNotNull(webService);

		// participant teams
		// are the teams of matchdata on the same order as in match?
		// reversed teams in matchdata is still accepted
		boolean isNotReversedTeams = matchService.isCandidateMatchTeam(match, matchdata.getTeam1().getTeamId(), 1) &&
				matchService.isCandidateMatchTeam(match, matchdata.getTeam2().getTeamId(), 2);
		boolean isReversedTeams = matchService.isCandidateMatchTeam(match, matchdata.getTeam1().getTeamId(), 2) &&
				matchService.isCandidateMatchTeam(match, matchdata.getTeam2().getTeamId(), 1);
		if (!isNotReversedTeams && !isReversedTeams) {
			logger.warn(String.format("There is problem with teams of matchdata and match with matchId=%d", match.getMatchId()));
			return false;
		}
				
		Long team1WsId = null;
		if (match.getTeam1() == null) {
			team1WsId = (long)(!isReversedTeams ? matchdata.getTeam1().getTeamId() : matchdata.getTeam2().getTeamId());
		}
		Long team2WsId = null;
		if (match.getTeam2() == null) {
			team2WsId = (long)(!isReversedTeams ? matchdata.getTeam2().getTeamId() : matchdata.getTeam1().getTeamId());
		}
		
		// match result
		Byte goalNormal1 = null;
		Byte goalNormal2 = null;
		Byte goalNormalExtra1 = null;
		Byte goalNormalExtra2 = null;
		Byte goalExtra1 = null;
		Byte goalExtra2 = null;
		Byte goalPenalty1 = null; 
		Byte goalPenalty2 = null;
		
		// retrieves results by the labels used in webService
		for (MatchResult matchResult : matchdata.getMatchResults()) {
			if (matchResult.getResultName().equals(webService.getResultNormalLabel())) {
				goalNormal1 = (!isReversedTeams ? matchResult.getPointsTeam1() : matchResult.getPointsTeam2()).byteValue();
				goalNormal2 = (!isReversedTeams ? matchResult.getPointsTeam2() : matchResult.getPointsTeam1()).byteValue();
			}
			else if (matchResult.getResultName().equals(webService.getResultNormalExtraLabel())) {
				goalNormalExtra1 = (!isReversedTeams ? matchResult.getPointsTeam1() : matchResult.getPointsTeam2()).byteValue();
				goalNormalExtra2 = (!isReversedTeams ? matchResult.getPointsTeam2() : matchResult.getPointsTeam1()).byteValue();
			}
			else if (matchResult.getResultName().equals(webService.getResultExtraLabel())) {
				goalExtra1 = (!isReversedTeams ? matchResult.getPointsTeam1() : matchResult.getPointsTeam2()).byteValue();
				goalExtra2 = (!isReversedTeams ? matchResult.getPointsTeam2() : matchResult.getPointsTeam1()).byteValue();
			}
			else if (matchResult.getResultName().equals(webService.getResultPenaltyLabel())) {
				goalPenalty1 = (!isReversedTeams ? matchResult.getPointsTeam1() : matchResult.getPointsTeam2()).byteValue();
				goalPenalty2 = (!isReversedTeams ? matchResult.getPointsTeam2() : matchResult.getPointsTeam1()).byteValue();
			}
		}
		
		// if goalNormalExtra[12] are given (see webService.resultNormalExtraLabel), the fields must be shuffled 
		if (goalNormalExtra1 != null && goalNormalExtra2 != null) {
            if (goalExtra1 != null && goalExtra2 != null) {
            	// goalNormal[12] contain in fact the result after penalties  
                goalPenalty1 = goalNormal1;
                goalPenalty2 = goalNormal2;
            }
            else{ // (there is no occurrence for this case)
            	// goalNormal[12] contain in fact the result after extra time
                goalExtra1 = goalNormal1;
                goalExtra2 = goalNormal2;
            }
            // result after 90 minutes
            goalNormal1 = goalNormalExtra1;
            goalNormal2 = goalNormalExtra2;
            
            // fields not used more
            goalNormalExtra1 = null;
            goalNormalExtra2 = null;
        }
		
		Match updatedMatch = matchService.updateMatchByMatchdata(match.getMatchId(), 
				team1WsId, team2WsId, goalNormal1, goalExtra1, goalPenalty1, 
				goalNormal2, goalExtra2, goalPenalty2); 

		return updatedMatch != null;
	}
}
