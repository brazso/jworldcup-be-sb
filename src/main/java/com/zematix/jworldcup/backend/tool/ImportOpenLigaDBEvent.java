package com.zematix.jworldcup.backend.tool;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Imports all matches and other data of a new event from OpeLigaDB. 
 * It would be exhaustive manually entering the start data of a tournament. 
 * Run its {@link ImportOpenLigaDBEvent#main(String[]) method to start the import
 * after your {@link ImportOpenLigaDBEventFactory} has the implementation.
  */
@SpringBootApplication
@EntityScan(basePackages = "com.zematix.jworldcup.backend.entity")
public class ImportOpenLigaDBEvent implements CommandLineRunner {

	//private static final Logger logger = LoggerFactory.getLogger(ImportOpenLigaDBEvent.class);
	
	@PersistenceUnit
	private EntityManagerFactory emf; // application managed transaction 
	
	@Inject
    private ConfigurableApplicationContext context;
	
	/**
	 * @param eventShortDescWithYear
	 * @param persistenceUnitName
	 * @param isTestMode - if true changes are not committed back to the database
	 */
	public void importOpenLigaDBEvent(String eventShortDescWithYear, boolean isTestMode) {
		// Obtains an entity manager (with application managed transaction) and a transaction
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		
		tx.begin();
		
		ImportOpenLigaDBEventFactory factory = new ImportOpenLigaDBEventFactory(eventShortDescWithYear);
		OpenLigaDBEvent openLigaDBEvent = factory.createOpenLigaDBEvent();
		openLigaDBEvent.setParams("EntityManager", em);
		openLigaDBEvent.setParams("TestMode", isTestMode);
		boolean isCommitable = openLigaDBEvent.importEvent();
		
		if (isCommitable) {
			tx.commit();
		}
		else {
			tx.rollback();
		}
		
		// Closes the entity manager
		em.close();
	}
	
	public static void main(String[] args) {
        SpringApplication.run(ImportOpenLigaDBEvent.class, args);
    }

	/**
	 * Starts import. Hard coded parameters are used for the time being.
	 * 
	 * @param args
	 */
    @Override
    public void run(String... args) {
//		final String eventShortDescWithYear = "CA2021";
		final String eventShortDescWithYear = "EC2020";
		final boolean isTestMode = false; // flag that changes are not committed back to the database

		importOpenLigaDBEvent(eventShortDescWithYear, isTestMode);

		System.exit(SpringApplication.exit(context));
    }
}
