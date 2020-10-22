package com.zematix.jworldcup.backend.tool;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

/**
 * Imports all matches and other data of a new event from OpeLigaDB. 
 * It would be exhaustive manually entering the start data of a tournament. 
 * Run its {@link ImportOpenLigaDBEvent#main(String[]) method to start the import
 * after your {@link ImportOpenLigaDBEventFactory} has the implementation.
  */
public class ImportOpenLigaDBEvent {

	//private static final Logger logger = LoggerFactory.getLogger(ImportOpenLigaDBEvent.class);

	/**
	 * @param eventShortDescWithYear
	 * @param persistenceUnitName
	 */
	public void importOpenLigaDBEvent(String eventShortDescWithYear, String persistenceUnitName) {
		// Obtains an entity manager and a transaction
		EntityManagerFactory emf = null;
		try {
			emf = Persistence.createEntityManagerFactory(persistenceUnitName);
		}
		catch (PersistenceException e) {
			System.err.println(String.format("Not found %s persistence unit name.", persistenceUnitName));
			System.exit(1);
			return;			
		}
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		
		tx.begin();
		
		ImportOpenLigaDBEventFactory factory = new ImportOpenLigaDBEventFactory(eventShortDescWithYear);
		OpenLigaDBEvent openLigaDBEvent = factory.createOpenLigaDBEvent();
		openLigaDBEvent.setParams("EntityManager", em);
		//openLigaDBEvent.setParams("TestMode", true); // does not commit changes to database
		boolean isCommitable = openLigaDBEvent.importEvent();
		
		if (isCommitable) {
			tx.commit();
		}
		else {
			tx.rollback();
		}
		
		// Closes the entity manager and the factory
		em.close();
		emf.close();
	}
	
	/**
	 * Starts import. Hard coded parameters are used for the time being.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
//		if (args.length != 1) {
//			System.out.println("Wrong usage, exactly 1 parameter is expected, eventShortDescWithYear.");
//			System.exit(1);
//			return;
//		}

//		final String eventShortDescWithYear = args[0];
		final String eventShortDescWithYear = "CA2019";
		final String persistenceUnitName = "jworldcupDevelopment";

		new ImportOpenLigaDBEvent().importOpenLigaDBEvent(eventShortDescWithYear, persistenceUnitName);
	}
}
