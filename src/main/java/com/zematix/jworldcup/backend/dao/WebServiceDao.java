package com.zematix.jworldcup.backend.dao;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQuery;
import com.zematix.jworldcup.backend.entity.QWebService;
import com.zematix.jworldcup.backend.entity.WebService;

/**
 * Database operations around {@link WebService} entities.
 */
@Component
@Transactional
public class WebServiceDao extends DaoBase {
	
	/**
	 * Returns a list of all {@link WebService} entities from database.
	 * 
	 * @return list of all {@link WebService} entities
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<WebService> getAllWebServices() {
		TypedQuery<WebService> query = getEntityManager().createNamedQuery("WebService.findAll", WebService.class);
		List<WebService> webServices = query.getResultList();
		return webServices;
	}

	/**
	 * Returns a list of found {@link WebService} instances which belongs to the given {@code eventId}.
	 * Returned list is orderedBy {@link WebService#getPriority()}.
	 *    
	 * @param eventId - filter
	 * @return list of {@link WebService} objects belongs to the given userId
	 * @throws IllegalArgumentException if any of the given parameters is invalid 
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<WebService> retrieveWebServicesByEvent(Long eventId) {
		List<WebService> webServices;
		
		checkNotNull(eventId);
		
		QWebService qWebService = QWebService.webService;
		JPAQuery<WebService> query = new JPAQuery<>(getEntityManager());
		webServices = query.from(qWebService)
		  .where(qWebService.event.eventId.eq(eventId)
		  		.and(qWebService.priority.gt((byte) 0)))
		  .orderBy(qWebService.priority.asc())
		  .fetch();
		
		return webServices;
	}
}
