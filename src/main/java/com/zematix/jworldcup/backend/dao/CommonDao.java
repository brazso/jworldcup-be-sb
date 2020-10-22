package com.zematix.jworldcup.backend.dao;

import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * General database operations.
 */
@Component
@Transactional
public class CommonDao extends DaoBase {
	
	// Basic JPA operations
	
	/**
	 * Makes an instance managed and persistent.
	 * 
	 * @param entity - entity instance
	 */
	public <ENTITYCLASS> void persistEntity(ENTITYCLASS entity) {
		getEntityManager().persist(entity);
	}

	/**
	 * Merges the state of the given detached entity into the current persistence context.
	 * 
	 * @param entity - entity instance
	 * @return updated and managed entity instance
	 */
	public <ENTITYCLASS> ENTITYCLASS mergeEntity(ENTITYCLASS entity) {
		return getEntityManager().merge(entity);
	}

	/**
	 * Removes entity from the database
	 * 
	 * @param entity - entity instance
	 */
	public <ENTITYCLASS> void removeEntity(ENTITYCLASS entity) {
		entity = getEntityManager().merge(entity);
		getEntityManager().remove(entity);
	}

	/**
	 * Refreshes the state of the instance from the database
	 * 
	 * @param entity - entity instance
	 */
	//@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public <ENTITYCLASS> void refreshEntity(ENTITYCLASS entity) {
		getEntityManager().refresh(entity);
	}

	/**
	 * Checks if the instance is a managed entity belonging to the 
	 * current persistence context.
	 * 
	 * @param entity - entity instance
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public <ENTITYCLASS> boolean containsEntity(ENTITYCLASS entity) {
		return getEntityManager().contains(entity);
	}

	/**
	 * Removes the given entity from the persistence context, 
	 * causing a managed entity to become detached.
	 * 
	 * @param entity - entity instance
	 */
	public <ENTITYCLASS> void detachEntity(ENTITYCLASS entity) {
		if (getEntityManager().contains(entity)) {
			getEntityManager().detach(entity);
		}
	}
	
	/**
	 * Synchronizes the persistence context to the underlying database. 
	 */
	public void flushEntityManager() {
		getEntityManager().flush();
	}

	// Common Queries
	
	/**
	 * Returns a list of all entities of the given entity class
	 * 
	 * @param entityClass
	 * @return list of all entities
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public <ENTITYCLASS> List<ENTITYCLASS> findAllEntities(Class<ENTITYCLASS> entityClass) {
		TypedQuery<ENTITYCLASS> q = getEntityManager()
				.createQuery("SELECT entity FROM " + entityClass.getSimpleName() + " entity", entityClass);
		return q.getResultList();
	}

	/**
	 * Returns an entity of the a given entity class matched by the given key value
	 * 
	 * @param entityClass
	 * @param entityId
	 * @return found entity or {@code null} otherwise
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public <ENTITYCLASS> ENTITYCLASS findEntityById(Class<ENTITYCLASS> entityClass, Object entityId) {
		return findEntityById(entityClass, entityId, /*refresh*/ false);
	}

	/**
	 * Returns an entity of a given entity class and key value where the result entity can be refreshed
	 * 
	 * @param entityClass
	 * @param entityId
	 * @param refresh - Refresh the found entity?
	 * @return found entity or {@code null} otherwise
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public <ENTITYCLASS> ENTITYCLASS findEntityById(Class<ENTITYCLASS> entityClass, Object entityId, boolean refresh) {
		ENTITYCLASS row = getEntityManager().find(entityClass, entityId);
		if (refresh && row != null)
			getEntityManager().refresh(row);
		return row;
	}

	/**
	 * Returns the ID value of the given JPA entity.
	 * 
	 * @param entity
	 * @return ID value of the entity
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public <ENTITYCLASS> Object getEntityIdentifier(ENTITYCLASS entity) {
		return getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity); 
	}

	/**
	 * Returns the ID field name of the given JPA entity class.
	 * 
	 * @param entityClass
	 * @return ID field name
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public <ENTITYCLASS> String getEntityIdentifierName(Class<ENTITYCLASS> entityClass) {
		Metamodel m = getEntityManager().getMetamodel();
		EntityType<ENTITYCLASS> entityType = m.entity(entityClass);
		final Type<?> idType = entityType.getIdType();
		final SingularAttribute<? super ENTITYCLASS, ?> idAttribute = entityType.getId(idType.getJavaType());
		return idAttribute.getName();
	}

	/**
	 * Returns the type of the ID field of a JPA entity.
	 *
	 * @param entityType
	 * @return type of ID field of the entity
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Class<?> getEntityIdentifierType(Class<?> entityType) {
		return getEntityManager().getMetamodel().entity(entityType).getIdType().getJavaType();
	}
}
