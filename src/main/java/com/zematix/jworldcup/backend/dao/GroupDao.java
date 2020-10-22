package com.zematix.jworldcup.backend.dao;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQuery;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Group;
import com.zematix.jworldcup.backend.entity.QGroup;

/**
 * Database operations around {@link Group} entities.
 */
@Component
@Transactional
public class GroupDao extends DaoBase {

	/**
	 * Returns a list of all {@link Group} entities from database.
	 * 
	 * @return list of all {@link Group} entities
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Group> getAllGroups() {
		TypedQuery<Group> query = getEntityManager().createNamedQuery("Group.findAll", Group.class);
		List<Group> groups = query.getResultList();
		return groups;
	}
	
	/**
	 * Returns a list of {@link Group} instances belongs to the given {@code eventId} parameter.
	 * The result list is sorted by the name of the groups. 
	 * If no {@link Event} instance belongs to the given {@code eventId} parameter, 
	 * empty list is the result.
	 * 
	 * @param eventId
	 * @return list of {@link Group} instances belongs to the given {@code eventId} parameter
	 * @throws IllegalArgumentException if given {@code eventId} is {@code null} 
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Group> retrieveGroupsByEvent(Long eventId) {
		List<Group> groups;
		
		QGroup qGroup = QGroup.group;
		JPAQuery<Group> query = new JPAQuery<>(getEntityManager());
		groups = query.from(qGroup)
			.where(qGroup.event.eventId.eq(eventId))
			.orderBy(qGroup.name.asc())
			.fetch();
		
		return groups;
	}

	/**
	 * Retrieves {@link Group} instance belongs to the given {@code eventId} and
	 *  group {@code name}.
	 * 
	 * @param eventId
	 * @param name - group name, for example "A"
	 * @return found {@Group} instance or {@code null} if not found
	 * @throws IllegalArgumentException if any of the parameters is null
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Group retrieveGroupByName(Long eventId, String name) {
		Group group = null;
		
		QGroup qGroup = QGroup.group;
		JPAQuery<Group> query = new JPAQuery<>(getEntityManager());
		group = query.from(qGroup)
			.where(qGroup.event.eventId.eq(eventId)
					.and(qGroup.name.eq(name)))
			.fetchFirst();
		
		return group;
	}
}
