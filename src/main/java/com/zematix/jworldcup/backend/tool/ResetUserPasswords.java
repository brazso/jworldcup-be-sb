package com.zematix.jworldcup.backend.tool;

import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDateTime;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnit;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;

import com.zematix.jworldcup.backend.crypto.SecureHashing;
import com.zematix.jworldcup.backend.entity.User;

/**
 * Reset all users' password that they become login name in lower case with "_!" postfix.
 * It makes testing easier. It runs only with -Dapplication.environment=develop
 * VM setting.
  */
@EnableAutoConfiguration
@EntityScan(basePackages = "com.zematix.jworldcup.backend.entity")
public class ResetUserPasswords implements CommandLineRunner {

	@PersistenceUnit
	private EntityManagerFactory emf; // application managed transaction 
	
	@Inject
	private ConfigurableApplicationContext context;

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
	}

	public static void main(String[] args) {
		SpringApplication.run(ResetUserPasswords.class, args);
	}

	/**
	 * Starts import. Hard coded parameters are used for the time being.
	 * 
	 * @param args
	 */
	@Override
	public void run(String... args) {
		main();

		System.exit(SpringApplication.exit(context));
	}

}
