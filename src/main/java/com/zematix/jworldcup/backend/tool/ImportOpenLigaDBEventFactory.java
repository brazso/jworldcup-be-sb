package com.zematix.jworldcup.backend.tool;

/**
 * Factory class for creation of {@link OpenLigaDBEvent} objects.
 * The latter is used to import a complete tournament skeleton.
 * @see <a href="https://www.openligadb.de">https://www.openligadb.de</a>
 */
public class ImportOpenLigaDBEventFactory {

	protected String eventShortDescWithYear;

	public ImportOpenLigaDBEventFactory(String eventShortDescWithYear) {
		super();
		this.eventShortDescWithYear = eventShortDescWithYear;
	}
	
	/**
	 * Creates a new OpenLigaDBEvent object. Add your new subclass to
	 * import a new tournament from OpenLigaDB.
	 * 
	 * @return a new OpenLigaDBEvent object
	 */
	public OpenLigaDBEvent createOpenLigaDBEvent() {
		switch (eventShortDescWithYear) {
		case "WC2018": 
			return new OpenLigaDBEventWC2018();
		case "AFC2019":
			return new OpenLigaDBEventAFC2019();
		case "CA2019":
			return new OpenLigaDBEventCA2019();
		case "CAF2019":
			return new OpenLigaDBEventCAF2019();
		case "EC2020": 
			return new OpenLigaDBEventEC2020();
		case "CA2021": 
			return new OpenLigaDBEventCA2021();
		default:
			return null;
		}
	}
}
