package com.zematix.jworldcup.backend.tool;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OpenLigaDBEvent {

	protected static final Logger logger = LoggerFactory.getLogger(ImportOpenLigaDBEvent.class);
	
	protected Map<String, Object> params = new HashMap<>();
	
	public OpenLigaDBEvent() {
	}

	protected <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
	    Set<Object> seen = ConcurrentHashMap.newKeySet();
	    return t -> seen.add(keyExtractor.apply(t));
	}

//	public Object getParams(String name) {
//		return params.get(name);
//	}

	/**
	 * Arbitrary parameters can be placed into {@link OpenLigaDBEvent#params} map.
	 * 
	 * @param name
	 * @param value
	 */
	public void setParams(String name, Object value) {
		params.put(name, value);
	}
	
	public abstract boolean importEvent();
}
