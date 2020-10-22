package com.zematix.jworldcup.backend.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * Utility interface for general functions. It contains only default methods. It is a 
 * replacement for utility class with static methods because the latter one is poorly testable.
 * Note: in an interface everything is public by default and all constants are static final
 * Usage can be done in either directly or indirectly implementing the interface. In the
 * latter case we need a helper implementation class and that one is used everywhere. In this
 * project, especially because of CDI environment, the indirect case is used.  
 */
public final class CommonUtil {

	public static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);
	
	public static final SecureRandom RANDOM = new SecureRandom();
	
	/**
	 * Implicit construction is disallowed.
	 */
	private CommonUtil() {
		throw new AssertionError();
	}

	/**
	 * Returns {@code true} if the given email address is valid
	 * 
	 * @param email - email address to be validated
	 * @return {@code true} if the given email address is valid, {@code false} otherwise
	 */
	public static boolean isEmailValid(String email) {
		if (email == null) {
			return false;
		}
		String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}

	/**
	 * Returns the encrypted hash code of the given loginPassword used by the
	 * application. See more info about used hashing algorithm at {@link SecureHashingOld}.
	 * 
	 * @param - loginName (not used)
	 * @param - loginPassword
	 * @return encrypted hashed password 
	 */
	public static String getEncryptedLoginPassword(String loginName, String loginPassword) {
		return SecureHashing.hashString(loginPassword);
	}

	/**
	 * Validates given loginPassword with the given loginPasswordHash. Returns {@code true}
	 * if they match.
	 *  
	 * @param loginPassword
	 * @param loginPasswordHash
	 * @return {@code true} if the given parameters match, {@code false} otherwise
	 */
	public static boolean validateLoginPassword(String loginPassword, String loginPasswordHash) {
		return SecureHashing.validateHash(loginPassword, loginPasswordHash);
	}

	/**
	 * Validates length of given loginPasswordHash with the value of used 
	 * in the hashing algorithm. Returns {@code true} if they match.
	 *  
	 * @param loginPassword
	 * @param loginPasswordHash
	 * @return {@code true} if the length of the given loginPasswordHash value length is correct,
	 *         {@code false} otherwise
	 */
	public static boolean validateLoginPasswordLength(String loginPasswordHash) {
		checkArgument(loginPasswordHash!=null, "Argument \"loginPasswordHash\" must not be null");
		return SecureHashing.getHashLength() == loginPasswordHash.length();
	}

	/**
	 * Generates a random String suitable for use as a token.
	 * It must be exactly 20 characters long and must contain only [a-zA-Z0-9] characters.
	 * 
	 * @return String suitable for use as a random token
	 */
	public static String generateRandomToken() {
		final int TOKEN_LENGTH = 20;
		String allowedCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

		StringBuilder token = new StringBuilder();
		for (int i = 0; i < TOKEN_LENGTH; i++) {
			int index = RANDOM.nextInt(allowedCharacters.length());
			token.append(allowedCharacters.substring(index, index + 1));
		}
		
		return token.toString();
	}

	/**
	 * Generates a random String suitable for use as a temporary password.
	 * The result is 12 characters long and omits some easily mistakable characters.
	 *
	 * @return String suitable for use as a temporary password
	 */
	public static String generateRandomPassword() {
		final int PASSWORD_LENGTH = 12;
		// Pick from some letters that won't be easily mistaken for each
		// other. So, for example, omit o O and 0, 1 l and L.
		String allowedCharacters = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789+@";

		StringBuilder password = new StringBuilder();
		for (int i = 0; i < PASSWORD_LENGTH; i++) {
			int index = RANDOM.nextInt(allowedCharacters.length());
			password.append(allowedCharacters.substring(index, index + 1));
		}
		
		return password.toString();
	}

	/**
	 * Capitalizes first letter of the given string.
	 * 
	 * @param data - string to be capitalized in its first letter
	 * @return First letter capitalized string
	 */
	public static String capitalizeFirstLetter(String data) {
		if (Strings.isNullOrEmpty(data)) {
			return data;
		}
		return data.substring(0, 1).toUpperCase() + data.substring(1);
	}
	
	/**
	 * Capitalizes first letters of all word of the given string.
	 * 
	 * @param data - string to be capitalized in its first letters of all words
	 * @return First letter capitalized words
	 */
	public static String capitalize(String data) {
		if (Strings.isNullOrEmpty(data)) {
			return data;
		}
		
		String result = Pattern.compile("\\s")
				.splitAsStream(data)
				.map(CommonUtil::capitalizeFirstLetter)
				.collect(Collectors.joining(" "));

		return result;
	}
	
	/**
	 * To the input {@code startTime} adds the given {@code minutes} and returns the result.
	 * 
	 * @param startTime - start datetime
	 * @param minutes - added minutes
	 * @return calculated {@link Date} instance which equals to given {@code startTime} + provided {@code minutes} in minutes
	 */
	public static LocalDateTime plusMinutes(LocalDateTime startTime, long minutes) {
		checkArgument(startTime!=null, "Parameter \"startTime\" value must not be null.");
		
		return startTime.plus(minutes, ChronoUnit.MINUTES);
	}
	
	/**
	 * To the input {@code startTime} adds the given {@code days} and returns the result.
	 * 
	 * @param startTime - start datetime
	 * @param days - added days
	 * @return calculated {@link Date} instance which equals to given {@code startTime} + provided {@code days} in days
	 */
	public static LocalDateTime plusDays(LocalDateTime startTime, long days) {
		checkArgument(startTime!=null, "Parameter \"startTime\" value must not be null.");
		
		return startTime.plus(days, ChronoUnit.DAYS);
	}

	/**
	 * Based on input {@code dateTime} returns a new date without time info.
	 * It does not alter the input {@code dateTime} parameter value.
	 * 
	 * @param dateTime - with date and time info
	 * @return calculated {@link LocalDateTime} instance where time info is removed from the given {@code dateTime} 
	 */
	public static LocalDateTime truncateDateTime(LocalDateTime dateTime) {
		checkArgument(dateTime!=null, "Parameter \"dateTime\" value must not be null.");
		
		return dateTime.truncatedTo(ChronoUnit.DAYS);
	}

	/**
	 * Return days between input date values. Inputs may contain time part, those are truncated,
	 * only their date parts take into account.
	 * 
	 * @param dateTimeStart
	 * @param dateTimeEnd
	 * @return days between input date values 
	 */
	public static long daysBetween(LocalDateTime dateTimeStart, LocalDateTime dateTimeEnd) {
		checkArgument(dateTimeStart!=null, "Parameter \"dateTimeStart\" value must not be null.");
		checkArgument(dateTimeEnd!=null, "Parameter \"dateTimeEnd\" value must not be null.");
		
		return ChronoUnit.DAYS.between(dateTimeStart.truncatedTo(ChronoUnit.DAYS), dateTimeEnd.truncatedTo(ChronoUnit.DAYS));
	}

	/**
	 * Obtains an instance of LocalDateTime from a text string using "yyyy-MM-dd HH:mm" formatter.
	 * 
	 * @param text the text to parse, not null
	 * @return the parsed local date-time, not null
	 * @throws DateTimeParseException if the text cannot be parsed 
	 */
	public static LocalDateTime parseDateTime(String text) {
		return LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
	}
	
	/**
	 * Obtains an instance of LocalDateTime from a text string using "yyyy-MM-dd" formatter.
	 * 
	 * @param text the text to parse, not null
	 * @return the parsed local date-time, not null
	 * @throws DateTimeParseException if the text cannot be parsed 
	 */
	public static LocalDateTime parseDateToDateTime(String text) {
		return LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
	}
	
	/**
	 * Obtains an instance of LocalDateTime containing 3000-01-01.
	 * 
	 * @return the local date-time containing 3000-01-01
	 */
	public static LocalDateTime getEpochDateTime() {
		return LocalDate.parse("3000-01-01", DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
	}

	/**
	 * Converts the given {@ link LocalDateTime} object to {@link XMLGregorianCalendar} one
	 * 
	 * @param date - to be converted
	 */
	public static XMLGregorianCalendar toXMLGregorianCalendar(LocalDateTime date) {
		if (date == null) {
			return null;
		}
		XMLGregorianCalendar xmlCalendar = null;
		try {
			xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(date.getYear(), date.getMonthValue(),
					date.getDayOfMonth(), date.getHour(), date.getMinute(), date.getSecond(), date.getNano() / 1000, 0);
		} catch (DatatypeConfigurationException e) {
			logger.error("XMLGregorianCalendar could not be instantiated", e);
		}
		return xmlCalendar;
	}

	/**
	 * Converts the given {@link XMLGregorianCalendar} object to {@link LocalDateTime} one
	 * 
	 * @param calendar - to be converted
	 */
	public static LocalDateTime toDate(XMLGregorianCalendar calendar) {
		if (calendar == null) {
			return null;
		}
		
		LocalDateTime date = LocalDateTime.of(
				calendar.getYear(), 
				calendar.getMonth(), 
				calendar.getDay(),
				calendar.getHour(),
				calendar.getMinute(),
				calendar.getSecond(),
				calendar.getMillisecond()*1000);
		
		return date;
	}


}
