package com.zematix.jworldcup.backend.dao;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQuery;
import com.zematix.jworldcup.backend.entity.QRole;
import com.zematix.jworldcup.backend.entity.Role;

/**
 * Database operations around {@link Role} entities.
 */
@Component
@Transactional
public class RoleDao extends DaoBase {
	
	/**
	 * @return list of all Role entities from database
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Role> getAllRoles() {
		TypedQuery<Role> query = getEntityManager().createNamedQuery("Role.findAll", Role.class);
		List<Role> roles = query.getResultList();
		return roles;
	}

	/**
	 * Return found {@link Role} instance which matches the given role string value. 
	 * Otherwise {@code null} is returned.
	 * 
	 * @param - sRole - searched role string
	 * @return found {@link Role} entity instance or {@code null} if not found 
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Role findRoleByRole(String sRole) {
		Role role = null;
		
		QRole qRole = QRole.role1;
		JPAQuery<Role> query = new JPAQuery<>(getEntityManager());
		role = query.from(qRole)
			.where(qRole.role.eq(sRole))
		  .fetchOne();
		
		return role;
	}
	
}
