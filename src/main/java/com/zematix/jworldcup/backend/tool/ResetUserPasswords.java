package com.zematix.jworldcup.backend.tool;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zematix.jworldcup.backend.cdi.ApplicationEnvironment;
import com.zematix.jworldcup.backend.crypto.SecureHashing;
import com.zematix.jworldcup.backend.entity.User;

/**
 * Reset all users' password that they become login name in lower case with "_!" postfix.
 * It makes testing easier. It runs only with -Dapplication.environment=development
 * VM setting.
  */
public class ResetUserPasswords {

	private static final Logger logger = LoggerFactory.getLogger(ResetUserPasswords.class);
	
	private EntityManager em = null;
	
	public EntityManager getEntityManager() {
		return em;
	}

	/**
	 * Modifies all user login passwords into simple ones.
	 * New password is loginName converted into lower case added with "_!" postfix.
	 */
	public boolean resetUserPasswords() {
		checkNotNull(getEntityManager());
		
		boolean isCommitable = true;
		LocalDateTime actualDateTime = LocalDateTime.now(); 
		
		TypedQuery<User> query = getEntityManager().createNamedQuery("User.findAll", User.class);
		List<User> users = query.getResultList();

		for (User user : users) {
			logger.info("User: " + user.getLoginName());
			// overwrites login password
			String loginPassword = user.getLoginName().toLowerCase() + "_!";
			SecureHashing secureHashing = new SecureHashing();
			loginPassword = secureHashing.hashString(loginPassword);
			user.setLoginPassword(loginPassword);
			user.setModificationTime(actualDateTime);
		}
		
		return isCommitable;
	}
	
	/**
	 * 
	 */
	public void main() {

		final String persistenceUnitName = "jworldcupDevelopment";

		String environment = System.getProperty(ApplicationEnvironment.APP_ENV_PARAMETER_NAME);
		checkNotNull(environment);
		checkState(environment.equalsIgnoreCase(ApplicationEnvironment.DEVELOPMENT.name()));
		
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
		/*EntityManager*/ em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		
		tx.begin();

		boolean isCommitable = resetUserPasswords();
		
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
	 * @param args
	 */
	public static void main(String[] args) {

		new ResetUserPasswords().main();
	}
}
