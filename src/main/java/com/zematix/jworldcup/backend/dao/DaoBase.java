package com.zematix.jworldcup.backend.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
