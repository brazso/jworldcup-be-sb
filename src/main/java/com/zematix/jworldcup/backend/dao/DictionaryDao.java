package com.zematix.jworldcup.backend.dao;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQuery;
import com.zematix.jworldcup.backend.configuration.CachingConfig;
import com.zematix.jworldcup.backend.entity.Dictionary;
import com.zematix.jworldcup.backend.entity.QDictionary;

/**
 * Database operations around {@link Dictionary} entities.
 */
@Component
@Transactional
public class DictionaryDao extends DaoBase {
	
	/**
	 * @return list of all Dictionary entities from database
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Dictionary> getAllDictionaries() {
		TypedQuery<Dictionary> query = getEntityManager().createNamedQuery("Dictionary.findAll", Dictionary.class);
		return query.getResultList();
	}

	/**
	 * Return found {@link Dictionary} instances which matches the given {@code key}
	 * value. Otherwise empty list is returned.
	 * 
	 * @param - sRole - searched role string
	 * @return found {@link Dictionary} list or empty list if not found 
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	@Cacheable(cacheNames = CachingConfig.CACHE_DICTIONARY_BY_KEY, key = "{#key}")
	public List<Dictionary> findDictionarisByKey(String key) {
		QDictionary qDictionary = QDictionary.dictionary;
		JPAQuery<Dictionary> query = new JPAQuery<>(getEntityManager());
		return query.from(qDictionary)
			.where(qDictionary.key.eq(key))
			.fetch();
	}

	/**
	 * Return found {@link Dictionary} instance which matches the given {@code key} 
	 * and {@code value} values. Otherwise {@code null} is returned.
	 * 
	 * @param key - searched key
	 * @param value - searched value
	 * @return found {@link Dictionary} entity instance or {@code null} if not found 
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	@Cacheable(cacheNames = CachingConfig.CACHE_DICTIONARY_BY_KEY_AND_VALUE, key = "{#key, #value}")
	public Dictionary findDictionaryByKeyAndValue(String key, String value) {
		QDictionary qDictionary = QDictionary.dictionary;
		JPAQuery<Dictionary> query = new JPAQuery<>(getEntityManager());
		return query.from(qDictionary)
			.where(qDictionary.key.eq(key).and(qDictionary.value.eq(value)))
		  .fetchOne();
	}
	
}
