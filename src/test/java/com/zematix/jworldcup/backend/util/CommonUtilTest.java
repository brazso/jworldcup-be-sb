package com.zematix.jworldcup.backend.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.boot.test.context.SpringBootTest;

import com.zematix.jworldcup.backend.crypto.SecureHashing;


/**
 * Contains test functions of {@link CommonUtil} class. 
 */
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({CommonUtil.class, SecureHashing.class})
@SpringBootTest
@Ignore("JDK17 throws InaccessibleObjectException")
public class CommonUtilTest {
	
//	private static final Logger logger = LoggerFactory.getLogger(CommonUtilTest.class);

	/**
	 * Test {@link CommonUtilService#isEmailValid()} method.
	 * Scenario: input is an accepted valid email address
	 */
	@Test
	public void /*static boolean*/ isEmailValidSuccessful(/*String email*/) {
		String email = "brazso@zematix.hu";

		boolean isEmailValid = CommonUtil.isEmailValid(email);
		assertTrue(String.format("Email %s should be a valid email.", email), isEmailValid);
	}

	/**
	 * Test {@link CommonUtilService#isEmailValid()} method.
	 * Scenario: input is an wrong email address
	 */
	@Test
	public void /*static boolean*/ isEmailValidUnsuccessful(/*String email*/) {
		String email = "brazso@@zematix.hu";
		boolean isEmailValid = CommonUtil.isEmailValid(email);
		assertFalse(String.format("Email %s should not be a valid email.", email), isEmailValid);
	}

	/**
	 * Test {@link CommonUtilService#isEmailValid()} method.
	 * Scenario: input is a null as email address
	 */
	@Test
	public void /*static boolean*/ isEmailValidUnsuccessfulNull(/*String email*/) {
		String email = null;
		boolean isEmailValid = CommonUtil.isEmailValid(email);
		assertFalse(String.format("Email %s should not be a valid email.", email), isEmailValid);
	}

//	/**
//	 * Test {@link CommonUtilService#getEncryptedLoginPassword()} method.
//	 * Scenario: returns expected result from mocked result
//	 */
//	@Test
//	public void /*static String*/ getEncryptedLoginPassword(/*String loginName, String loginPassword*/) {
//		String loginName = "12345678";
//		String loginPassword = "12345678_!";
//		String expectedEncryptedLoginPassword = "a3a4";
//
//		//PowerMockito.mockStatic(SecureHashing.class); // mocks all static methods of the class
//		PowerMockito.spy(SecureHashing.class); // mocks only those methods of the class which are stubbed out manually later
//		Mockito.when(SecureHashing.hashString(loginPassword)).thenReturn(expectedEncryptedLoginPassword);
//
//		String encryptedLoginPassword = CommonUtil.getEncryptedLoginPassword(loginName, loginPassword);
//		assertEquals("Result should be equal to the expected one.", expectedEncryptedLoginPassword, encryptedLoginPassword);
//		
//		// there is not more test here, it just simply logs hashed passwords of all test users
//		loginName = "admin";
//		loginPassword = "admin_!";
//		//Mockito.when(SecureHashing.hashString(loginPassword)).thenCallRealMethod();
//		encryptedLoginPassword = CommonUtil.getEncryptedLoginPassword(loginName, loginPassword);
//		logger.info(String.format("EncryptedLoginPassword of test user \"%s/%s\" is %s", loginName, loginPassword, encryptedLoginPassword));
//		
//		loginName = "normal";
//		loginPassword = "normal_!";
//		encryptedLoginPassword = CommonUtil.getEncryptedLoginPassword(loginName, loginPassword);
//		logger.info(String.format("EncryptedLoginPassword of test user \"%s/%s\" is %s", loginName, loginPassword, encryptedLoginPassword));
//		
//		loginName = "candidate";
//		loginPassword = "candidate_!";
//		encryptedLoginPassword = CommonUtil.getEncryptedLoginPassword(loginName, loginPassword);
//		logger.info(String.format("EncryptedLoginPassword of test user \"%s/%s\" is %s", loginName, loginPassword, encryptedLoginPassword));
//
//		loginName = "locked";
//		loginPassword = "locked_!";
//		encryptedLoginPassword = CommonUtil.getEncryptedLoginPassword(loginName, loginPassword);
//		logger.info(String.format("EncryptedLoginPassword of test user \"%s/%s\" is %s", loginName, loginPassword, encryptedLoginPassword));
//		
//		loginName = "brazso";
//		loginPassword = "brazso_!";
//		encryptedLoginPassword = CommonUtil.getEncryptedLoginPassword(loginName, loginPassword);
//		logger.info(String.format("EncryptedLoginPassword of test user \"%s/%s\" is %s", loginName, loginPassword, encryptedLoginPassword));
//	}
//	
//	/**
//	 * Test {@link CommonUtilService#validateLoginPassword(String, String)} method.
//	 * Scenario: returns {@code true} from mocked result
//	 */
//	@Test
//	public /*static boolean*/ void validateLoginPassword(/*String loginPassword, String loginPasswordHash*/) {
//		String loginPassword = "12345678";
//		String loginPasswordHash = "a3a4";
//		boolean expectedValid = true;
//		PowerMockito.mockStatic(SecureHashing.class); // mocks all static methods of the class
//		Mockito.when(SecureHashing.validateHash(loginPassword, loginPasswordHash)).thenReturn(expectedValid);
//		
//		boolean valid = CommonUtil.validateLoginPassword(loginPassword, loginPasswordHash);
//		assertEquals("Result should be equal to the expected one.", expectedValid, valid);
//	}
//	
//	/**
//	 * Test {@link CommonUtilService#validateLoginPassword(String, String)} method.
//	 * Scenario: returns {@code false} from mocked result
//	 */
//	@Test
//	public /*static boolean*/ void validateLoginPassword_False(/*String loginPassword, String loginPasswordHash*/) {
//		String loginPassword = "12345678";
//		String loginPasswordHash = "a3a4";
//		boolean expectedValid = false;
//		PowerMockito.mockStatic(SecureHashing.class); // mocks all static methods of the class
//		Mockito.when(SecureHashing.validateHash(loginPassword, loginPasswordHash)).thenReturn(expectedValid);
//		
//		boolean valid = CommonUtil.validateLoginPassword(loginPassword, loginPasswordHash);
//		assertEquals("Result should be equal to the expected one.", expectedValid, valid);
//	}
//	
//	/**
//	 * Test {@link CommonUtilService#validateLoginPasswordLength(String)} method.
//	 * Scenario: returns {@code false} from mocked result
//	 */
//	@Test
//	public /*static boolean*/ void validateLoginPasswordLength(/*String loginPasswordHash*/) {
//		String loginPasswordHash = "a3a4";
//		boolean expectedValid = false;
//		PowerMockito.mockStatic(SecureHashing.class); // mocks all static methods of the class
//		Mockito.when(SecureHashing.getHashLength()).thenReturn(128);
//		
//		boolean valid = CommonUtil.validateLoginPasswordLength(loginPasswordHash);
//		assertEquals("Result should be equal to the expected one.", expectedValid, valid);
//	}
//	
//	/**
//	 * Test {@link CommonUtilService#validateLoginPasswordLength(String)} method.
//	 * Scenario: throws {@link IllegalArgumentException} because the given 
//	 *           loginPasswordHash parameter is {@code null}
//	 */
//	@Test(expected=IllegalArgumentException.class)
//	public /*static boolean*/ void validateLoginPasswordLength_Null(/*String loginPasswordHash*/) {
//		String loginPasswordHash = null;
//		CommonUtil.validateLoginPasswordLength(loginPasswordHash);
//	}
//	
	/**
	 * Test {@link CommonUtil#generateRandomToken()} method.
	 * Test token generation. It must be exactly 20 characters long and 
	 * must contain only [a-zA-Z0-9] characters.
	 * Scenario: good case
	 */
	@Test
	public void /*static String*/ generateRandomToken() {
		final int TOKEN_LENGTH = 20;
		
		String token = CommonUtil.generateRandomToken();
		assertEquals(String.format("Token must be exactly %d characters long", TOKEN_LENGTH), 
				TOKEN_LENGTH, token.length());

		String regex = "^[a-zA-Z0-9]+$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(token);
		assertTrue("Token must contain only word characters except underscore.", matcher.matches());
	}

	/**
	 * Test {@link CommonUtil#generateRandomPassword()} method.
	 * Random password generation. It must be exactly 12 characters long and 
	 * must contain only special characters.
	 * Scenario: good case
	 */
	@Test
	public void /*String*/ generateRandomPassword() {
		final int PASSWORD_LENGTH = 12;
		String allowedCharacters = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789+@";
		String randomPassword = CommonUtil.generateRandomPassword();

		assertEquals(String.format("Random password must be exactly %d characters long", PASSWORD_LENGTH), 
				PASSWORD_LENGTH, randomPassword.length());

		String regex = "^["+allowedCharacters+"]+$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(randomPassword);
		assertTrue(String.format("Random password must contain only specified \"%s\" characters.", allowedCharacters), matcher.matches());
	}

	/**
	 * Test {@link CommonUtil#capitalizeFirstLetter(String)} method.
	 * Capitalizes first letter of the given string. 
	 * Scenario: good case
	 */
	@Test
	public void /*String*/ capitalizeFirstLetter(/*String data*/) {
		String data = "camel";
		String expectedResult = "Camel";
		
		String result = CommonUtil.capitalizeFirstLetter(data);
		assertEquals("Capitalized first letter string differs from the expected one.", expectedResult, result);
	}
	
	/**
	 * Test {@link CommonUtil#capitalizeFirstLetter(String)} method.
	 * Capitalizes first letter of the given string. 
	 * Scenario: good case
	 */
	@Test
	public void /*String*/ capitalizeFirstLetterOne(/*String data*/) {
		String data = "c";
		String expectedResult = "C";
		
		String result = CommonUtil.capitalizeFirstLetter(data);
		assertEquals("Capitalized first letter string differs from the expected one.", expectedResult, result);
	}
	
	/**
	 * Test {@link CommonUtil#capitalizeFirstLetter(String)} method.
	 * Capitalizes first letter of the given string. 
	 * Scenario: good case
	 */
	@Test
	public void /*String*/ capitalizeFirstLetterNull(/*String data*/) {
		String data = null;
		String expectedResult = null;
		
		String result = CommonUtil.capitalizeFirstLetter(data);
		assertEquals("Capitalized first letter string differs from the expected one.", expectedResult, result);
	}
	
	/**
	 * Test {@link CommonUtil#capitalize(String)} method.
	 * Capitalizes first letters of all words of the given string. 
	 * Scenario: good case
	 */
	@Test
	public void /*String*/ capitalize(/*String data*/) {
		String data = "have you   2 pieces of bucket?";
		String expectedCapitalizedData = "Have You   2 Pieces Of Bucket?";

		String capitalizedData = CommonUtil.capitalize(data);
		assertEquals("Capitalized result differs from the expected one.", expectedCapitalizedData, capitalizedData);
	}
	
	/**
	 * Test {@link CommonUtil#plusMinutes(Date, long)} method.
	 * Scenario: fails with exception because of {@code null} {@code startTime} argument
	 */
	@Test(expected=NullPointerException.class)
	public /*static*/ void /*Date*/ plusMinutesNull(/*Date startTime, long minutes*/) {
		LocalDateTime startTime = null;
		long minutes = 0;
		CommonUtil.plusMinutes(startTime, minutes);
	}

	/**
	 * Test {@link CommonUtil#plusMinutes(Date, long)} method.
	 * Scenario: successfully adding 0 minute
	 * @throws ParseException 
	 */
	@Test
	public /*static*/ void /*Date*/ plusMinutesZero(/*Date startTime, long minutes*/) throws ParseException {
		LocalDateTime startTime = LocalDateTime.parse("2017-02-22 11:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		long minutes = 0L;
		LocalDateTime expectedEndTime = startTime;
		LocalDateTime endTime = CommonUtil.plusMinutes(startTime, minutes);
		assertEquals("Calculated end time should be equal to the expected one.", expectedEndTime, endTime);
	}

	/**
	 * Test {@link CommonUtil#plusMinutes(Date, long)} method.
	 * Scenario: successfully adding 105 minutes
	 * @throws ParseException 
	 */
	@Test
	public /*static*/ void /*Date*/ plusMinutes105(/*Date startTime, long minutes*/) throws ParseException {
		LocalDateTime startTime = LocalDateTime.parse("2017-02-22 11:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		long minutes = 105L;
		LocalDateTime expectedEndTime = LocalDateTime.parse("2017-02-22 12:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		LocalDateTime endTime = CommonUtil.plusMinutes(startTime, minutes);
		assertEquals("Calculated end time should be equal to the expected one.", expectedEndTime, endTime);
	}

	/**
	 * Test {@link CommonUtil#plusDays(Date, long)} method.
	 * Scenario: fails with exception because of {@code null} {@code startTime} argument
	 */
	@Test(expected=NullPointerException.class)
	public /*static*/ void /*Date*/ plusDaysNull(/*Date startTime, long mays*/) {
		LocalDateTime startTime = null;
		long days = 0;
		CommonUtil.plusDays(startTime, days);
	}

	/**
	 * Test {@link CommonUtil#plusDays(Date, long)} method.
	 * Scenario: successfully adding 0 day
	 * @throws ParseException 
	 */
	@Test
	public /*static*/ void /*Date*/ plusDaysZero(/*Date startTime, long mays*/) throws ParseException {
		LocalDateTime startTime = LocalDateTime.parse("2017-02-22 11:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		long days = 0;
		LocalDateTime expectedEndTime = startTime;
		LocalDateTime endTime = CommonUtil.plusDays(startTime, days);
		assertEquals("Calculated end time should be equal to the expected one.", expectedEndTime, endTime);
	}

	/**
	 * Test {@link CommonUtil#plusDays(Date, long)} method.
	 * Scenario: successfully adding 30 days
	 * @throws ParseException 
	 */
	@Test
	public /*static*/ void /*Date*/ plusDays30(/*Date startTime, long mays*/) throws ParseException {
		LocalDateTime startTime = LocalDateTime.parse("2017-02-22 11:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		long days = 30;
		LocalDateTime expectedEndTime = LocalDateTime.parse("2017-03-24 11:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		LocalDateTime endTime = CommonUtil.plusDays(startTime, days);
		assertEquals("Calculated end time should be equal to the expected one.", expectedEndTime, endTime);
	}

	/**
	 * Test {@link CommonUtil#truncateDateTime(Date)} method.
	 * Scenario: fails with exception because of {@code null} {@code dateTime} argument
	 */
	@Test(expected=NullPointerException.class)
	public /*static*/ void /*Date*/ truncateDateTimeNull(/*Date dateTime*/) {
		LocalDateTime dateTime = null;
		CommonUtil.truncateDateTime(dateTime);
	}

	/**
	 * Test {@link CommonUtil#truncateDateTime(Date)} method.
	 * Scenario: successfully truncates date time
	 * @throws ParseException 
	 */
	@Test
	public /*static*/ void /*Date*/ truncateDateTime(/*Date dateTime*/) throws ParseException {
		LocalDateTime dateTime = LocalDateTime.parse("2017-02-22 11:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		LocalDateTime truncatedDateTime = CommonUtil.truncateDateTime(dateTime);
		LocalDateTime expectedDateTime = LocalDateTime.parse("2017-02-22 00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		assertEquals("Truncated date time should be equal to the expected one.", expectedDateTime, truncatedDateTime);
	}

	/**
	 * Test {@link CommonUtil#daysBetween(Date, Date)} method.
	 * Scenario: fails with exception because of {@code null} {@code dateTimeStart} argument
	 */
	@Test(expected=NullPointerException.class)
	public /*static long*/ void daysBetween_NullDateTimeStart(/*Date dateTimeStart, Date dateTimeEnd*/) throws ParseException {
		LocalDateTime dateTimeStart = null;
		LocalDateTime dateTimeEnd = LocalDateTime.parse("2017-02-22 11:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		CommonUtil.daysBetween(dateTimeStart, dateTimeEnd);
	}

	/**
	 * Test {@link CommonUtil#daysBetween(Date, Date)} method.
	 * Scenario: fails with exception because of {@code null} {@code dateTimeEnd} argument
	 */
	@Test(expected=NullPointerException.class)
	public /*static long*/ void daysBetween_NullDateTimeEnd(/*Date dateTimeStart, Date dateTimeEnd*/) throws ParseException {
		LocalDateTime dateTimeStart = LocalDateTime.parse("2017-02-22 11:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		LocalDateTime dateTimeEnd = null;
		CommonUtil.daysBetween(dateTimeStart, dateTimeEnd);
	}

	/**
	 * Test {@link CommonUtil#daysBetween(Date, Date)} method.
	 * Scenario: successfully returns result
	 */
	@Test
	public /*static long*/ void daysBetween_0(/*Date dateTimeStart, Date dateTimeEnd*/) throws ParseException {
		LocalDateTime dateTimeStart = LocalDateTime.parse("2017-02-22 11:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		LocalDateTime dateTimeEnd = LocalDateTime.parse("2017-02-22 11:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		
		long expectedDaysBetween = 0;
		long daysBetween = CommonUtil.daysBetween(dateTimeStart, dateTimeEnd);
		assertEquals("Result should be equal to the expected one.", expectedDaysBetween, daysBetween);
	}

	/**
	 * Test {@link CommonUtil#daysBetween(Date, Date)} method.
	 * Scenario: successfully returns result
	 */
	@Test
	public /*static long*/ void daysBetween_1(/*Date dateTimeStart, Date dateTimeEnd*/) throws ParseException {
		LocalDateTime dateTimeStart = LocalDateTime.parse("2017-02-22 11:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		LocalDateTime dateTimeEnd = LocalDateTime.parse("2017-02-23 10:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		
		long expectedDaysBetween = 1;
		long daysBetween = CommonUtil.daysBetween(dateTimeStart, dateTimeEnd);
		assertEquals("Result should be equal to the expected one.", expectedDaysBetween, daysBetween);
	}

	/**
	 * Test {@link CommonUtil#daysBetween(Date, Date)} method.
	 * Scenario: successfully returns result
	 */
	@Test
	public /*static long*/ void daysBetween_minus1(/*Date dateTimeStart, Date dateTimeEnd*/) throws ParseException {
		LocalDateTime dateTimeStart = LocalDateTime.parse("2017-02-23 10:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		LocalDateTime dateTimeEnd = LocalDateTime.parse("2017-02-22 11:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		
		long expectedDaysBetween = -1;
		long daysBetween = CommonUtil.daysBetween(dateTimeStart, dateTimeEnd);
		assertEquals("Result should be equal to the expected one.", expectedDaysBetween, daysBetween);
	}
	
	/**
	 * Test {@link CommonUtil#toXMLGregorianCalendar(LocalDateTime)} method.
	 * Scenario: successfully returns {@code null} if the given {@code date} parameter 
	 *           is {@code null}
	 */
	@Test
	public /*default XMLGregorianCalendar*/ void toXMLGregorianCalendar_NullDate(/*LocalDateTime date*/) {
		LocalDateTime date = null;
		XMLGregorianCalendar expectedCalendar = null;
		XMLGregorianCalendar calendar = CommonUtil.toXMLGregorianCalendar(date);
		assertEquals("Result should be equal to the expected one.", expectedCalendar, calendar);
	}
	
	/**
	 * Test {@link CommonUtil#toXMLGregorianCalendar(LocalDateTime)} method.
	 * Scenario: successfully returns result
	 * @throws ParseException 
	 * org.junit.ComparisonFailure: Result should be equal to the expected one. expected:<...7-02-22T11:00:20.000[Z]> but was:<...7-02-22T11:00:20.000[+01:00]>
	 */
	@Test
	public /*default XMLGregorianCalendar*/ void toXMLGregorianCalendar(/*LocalDateTime date*/) throws ParseException {
		LocalDateTime date = LocalDateTime.parse("2017-02-22 11:00:20", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		String expectedCalendarString = "2017-02-22T11:00:20.000Z";
		XMLGregorianCalendar calendar = CommonUtil.toXMLGregorianCalendar(date);
		assertEquals("Result should be equal to the expected one.", expectedCalendarString, calendar.toString());
	}
	
	/**
	 * Test {@link CommonUtil#toDate(XMLGregorianCalendar)} method.
	 * Scenario: successfully returns {@code null} if the given {@code calendar} parameter 
	 *           is {@code null}
	 */
	@Test
	public /*default LocalDateTime*/ void toDate_NullCalendar(/*XMLGregorianCalendar calendar*/) {
		XMLGregorianCalendar calendar = null;
		LocalDateTime expectedDate = null;
		LocalDateTime date = CommonUtil.toDate(calendar);
		assertEquals("Result should be equal to the expected one.", expectedDate, date);
	}

	/**
	 * Test {@link CommonUtil#toDate(XMLGregorianCalendar)} method.
	 * Scenario: successfully returns result
	 * @throws ParseException 
	 * @throws DatatypeConfigurationException 
	 */
	@Test
	public /*default LocalDateTime*/ void toDate(/*XMLGregorianCalendar calendar*/) throws ParseException {
		LocalDateTime expectedDate = LocalDateTime.parse("2017-02-22 11:00:20", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		XMLGregorianCalendar calendar = CommonUtil.toXMLGregorianCalendar(expectedDate);
		LocalDateTime date = CommonUtil.toDate(calendar);
		assertEquals("Result should be equal to the expected one.", expectedDate, date);
	}
}
