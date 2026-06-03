package com.zematix.jworldcup.backend.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.zematix.jworldcup.backend.service.ServerBase;

/**
 * Common ancestor of Dao classes.
 */
public abstract class DaoBase extends ServerBase {
	
	@PersistenceContext
	private EntityManager em;

	/**
	 * @return active EntityManager instance
	 */
	public EntityManager getEntityManager() {
		return em;
	}
}
