package com.zematix.jworldcup.backend.dao;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQuery;
import com.zematix.jworldcup.backend.entity.QUserStatus;
import com.zematix.jworldcup.backend.entity.UserStatus;

/**
 * Database operations around {@link UserStatus} entities.
 */
@Component
@Transactional
public class UserStatusDao extends DaoBase {
	
	/**
	 * @return list of all {@link UserStatus} entities from database
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<UserStatus> getAllUserStatuses() {
		TypedQuery<UserStatus> query = getEntityManager().createNamedQuery("UserStatus.findAll", UserStatus.class);
		List<UserStatus> userStatuses = query.getResultList();
		return userStatuses;
	}

	/**
	 * Returns found {@link UserStatus} instance which matches the given {@code status} string value. 
	 * Otherwise {@code null} is returned.
	 * 
	 * @param - status - searched status string
	 * @return found {@link UserStatus} entity instance with the given {@code status} field value 
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public UserStatus findUserStatusByStatus(String status) {
		UserStatus userStatus = null;
		
		QUserStatus qUserStatus = QUserStatus.userStatus;
		JPAQuery<UserStatus> query = new JPAQuery<>(getEntityManager());
		userStatus = query.from(qUserStatus)
			.where(qUserStatus.status.eq(status))
		  .fetchOne();
		
		return userStatus;
	}
}
