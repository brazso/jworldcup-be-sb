package com.zematix.jworldcup.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CountryCode {
	@JsonProperty("CLDR display name")
	private String displayName; // "Taiwan";
//	private String Capital; // "Taipei"
//	private String Continent; // "AS"
//	private String DS; // "RC"
//	@JsonProperty("")
//	private String "Developed / Developing Countries"; // null 
//	private String Dial; // "886"
//	private String EDGAR; // null
	@JsonProperty("FIFA")
	private String fifa; //"TPE"
//	private String FIPS; // "TW"
//	private String GAUL; // "925"
//	@JsonProperty("Geoname ID")
//	private String Geoname_ID; // 1668284.0
//	@JsonProperty("Global Code")
//	private String "Global Code"; // null
//	@JsonProperty("Global Name")
//	private String "Global Name"; // null
//	private String IOC; // "TPE"
//	private String "ISO3166-1-Alpha-2"; // "TW"
//	private String "ISO3166-1-Alpha-3"; // "TWN"
//	private String ISO3166-1-numeric; //"158"
//	private String ISO4217-currency_alphabetic_code; // null
//	private String "ISO4217-currency_country_name"; // null
//	private String "ISO4217-currency_minor_unit"; // null
//	private String "ISO4217-currency_name"; // null
//	private String "ISO4217-currency_numeric_code"; // null
//	private String "ITU"; // null
//	private String "Intermediate Region Code"; // null
//	private String "Intermediate Region Name"; // null
//	private String "Land Locked Developing Countries (LLDC)"; // null
//	private String "Languages"; // "zh-TW,zh,nan,hak"
//	private String "Least Developed Countries (LDC)"; // null
//	private String "M49"; // null
//	private String "MARC"; // "ch"
//	private String "Region Code"; // null
//	private String "Region Name"; // null
//	private String "Small Island Developing States (SIDS)"; // null
//	private String "Sub-region Code"; // null
//	private String "Sub-region Name"; // null
//	private String "TLD"; // ".tw"
//	private String "UNTERM Arabic Formal"; // null
//	private String "UNTERM Arabic Short"; // null
//	private String "UNTERM Chinese Formal"; // null
//	private String "UNTERM Chinese Short"; // null
//	private String "UNTERM English Formal"; // null
//	private String "UNTERM English Short"; // null
//	private String "UNTERM French Formal"; // null
//	private String "UNTERM French Short"; // null
//	private String "UNTERM Russian Formal"; // null
//	private String "UNTERM Russian Short"; // null
//	private String "UNTERM Spanish Formal"; // null
//	private String "UNTERM Spanish Short"; // null
//	private String "WMO"; // null
//	private String "is_independent"; // "Yes"
//	private String "official_name_ar"; // null
//	private String "official_name_cn"; // null
	@JsonProperty("official_name_en")
	private String name; // null
//	private String "official_name_es"; // null
//	private String "official_name_fr"; // null
//	private String "official_name_ru"; // null
	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}
	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	/**
	 * @return the fifa
	 */
	public String getFifa() {
		return fifa;
	}
	/**
	 * @param fifa the fifa to set
	 */
	public void setFifa(String fifa) {
		this.fifa = fifa;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

}
