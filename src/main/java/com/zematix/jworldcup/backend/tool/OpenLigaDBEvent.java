package com.zematix.jworldcup.backend.tool;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.zematix.jworldcup.backend.model.CountryCode;
import com.zematix.jworldcup.backend.service.OpenLigaDBService;

public abstract class OpenLigaDBEvent {

	protected static final Logger logger = LoggerFactory.getLogger(OpenLigaDBEvent.class);
	
	protected Map<String, Object> params = new HashMap<>();
	
	protected OpenLigaDBService openLigaDBService;
	
	public OpenLigaDBEvent() {
		this.openLigaDBService = new OpenLigaDBService(); // cannot be injected here
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
	
	/**
	 * Helper method retrieves FIFA country codes from a JSON url and returns a map containing 
	 * the result, where key is the country name in English, value is the 3 characters length 
	 * FIFA code.
	 * 
	 * @return map containing fifa codes by country names
	 */
	protected Map<String, String> retrieveFifaCodeByCountryNameMap(){
		final String COUNTRY_CODES_JSON_URL = "https://datahub.io/core/country-codes/r/country-codes.json";
		
		ObjectMapper mapper = new ObjectMapper();
		// not all properties will be read from json
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		List<CountryCode> list = new ArrayList<>();
		try {
			list = mapper.readValue(new URL(COUNTRY_CODES_JSON_URL), new TypeReference<List<CountryCode>>(){});
		} catch (IOException e) {
			logger.error("Country codes JSON file could not be read.", e);
			return null;
		}
		
		Map<String, String> mapByDisplayName = list.stream().filter(e -> !Strings.isNullOrEmpty(e.getDisplayName()) && !Strings.isNullOrEmpty(e.getFifa()))
				.collect(Collectors.toMap(CountryCode::getDisplayName, CountryCode::getFifa));
		Map<String, String> mapByName = list.stream().filter(e -> !Strings.isNullOrEmpty(e.getName()) && !Strings.isNullOrEmpty(e.getFifa()))
				.collect(Collectors.toMap(CountryCode::getName, CountryCode::getFifa));
		mapByName.putAll(mapByDisplayName);
		
		// add additional missing pairs
		mapByName.put("Scotland", "SCO");
		
		return mapByName;
	}

	/**
	 * Returns a German-English dictionary for missing team names.
	 * @return
	 */
	protected Map<String, String> retrieveTeamDictionary() {
		return Map.of(
				"Schweden","Sweden" 
				);
	}

}
