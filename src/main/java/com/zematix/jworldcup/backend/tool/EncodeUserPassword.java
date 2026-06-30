package com.zematix.jworldcup.backend.tool;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import com.zematix.jworldcup.backend.crypto.SecureHashing;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

/**
 * Encode given user password. By its help, you can check the generated hash code.
 * You may get different hash code to the very same password.
 */
@EnableAutoConfiguration
public class EncodeUserPassword implements CommandLineRunner {

	@Inject
	private ConfigurableApplicationContext context;

	private static final Logger logger = LoggerFactory.getLogger(EncodeUserPassword.class);

	/**
	 * Encode given user password
	 */
	public void encodeUserPassword() {
		System.out.println("Enter password!");
        Scanner scanner = new Scanner(System.in);
        String password = scanner.nextLine();
		logger.info("Input password: \"{}\"", password);

		// overwrites login password
		SecureHashing secureHashing = new SecureHashing();
		String encodedPassword = secureHashing.hashString(password);
		logger.info("Encoded password: {}", encodedPassword);
	}
	
	/**
	 * 
	 */
	public void main() {
		encodeUserPassword();
	}

	public static void main(String[] args) {
		SpringApplication.run(EncodeUserPassword.class, args);
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
