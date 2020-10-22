package com.zematix.jworldcup.backend.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.springframework.boot.test.context.SpringBootTest;

import com.zematix.jworldcup.backend.util.SecureHashing.HashAlgorithm;

/**
 * Contains test functions of {@link SecureHashing} class.
 * All Hashing functions use only {@link HashAlgorithm#SHA384} hashing algorithm.
 * 
 *  @see https://hash.online-convert.com/sha384-generator
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(SecureHashing.class)
@SpringBootTest
public class SecureHashingTest {

	/**
	 * Initializes constants used in our tests
	 */
	@Before
	public void initialize() {
		Whitebox.setInternalState(SecureHashing.class, "HASH_ALGORITHM", HashAlgorithm.SHA384);
		Whitebox.setInternalState(SecureHashing.class, "SALT_SIZE", 128);
		Whitebox.setInternalState(SecureHashing.class, "HASHING_LOG2_ROUNDS", 10);
	}
	
	/**
	 * Test {@link SecureHashing#hashString(String)} method.
	 * Scenario: creates hash successfully using fix salt. 
	 */
	@Test
	public void /*static String*/ hashString(/*String text*/) {
		//PowerMockito.mockStatic(SecureHashing.class); // mocks all static methods of the class
		PowerMockito.spy(SecureHashing.class); // mocks only some methods of the class
		String text = "12345678";
		String salt = "d883eb377dc74565e406c4ce1899c6e6";
		Mockito.when(SecureHashing.generateSalt()).thenReturn(salt);
		String hash = SecureHashing.hashString(text);
		String expectedHash = salt + "794182a2424c25d9a69d60ed5cb02d1109271c070554d49c551d2ac4c771beee6592f3701f29bc50eff676f9338b40f9";
		assertEquals(expectedHash, hash);
	}

	/**
	 * Test {@link SecureHashing#hashString(String)} method.
	 * Scenario: creates hash successfully using fix salt in 2^^0 round. 
	 */
	@Test
	public void /*static String*/ hashStringOnce(/*String text*/) {
		PowerMockito.spy(SecureHashing.class);
		String text = "12345678";
		String salt = "d883eb377dc74565e406c4ce1899c6e6";
		Mockito.when(SecureHashing.generateSalt()).thenReturn(salt);
		Whitebox.setInternalState(SecureHashing.class, "HASHING_LOG2_ROUNDS", 0);
		String hash = SecureHashing.hashString(text);
		String expectedHash = salt + "eb34787296648963a193bcddc98b5352a9dcd12e4b30463cfa749be9508562fc430576d3f6a3752552bdf2e9c849a39e";
		assertEquals(expectedHash, hash);
	}
	
	/**
	 * Test {@link SecureHashing#hashString(String)} method.
	 * Scenario: creates hash successfully without salt in 2^^0 round. 
	 */
	@Test
	public void /*static String*/ hashStringOnceWoSalt(/*String text*/) {
		String text = "12345678";
		Whitebox.setInternalState(SecureHashing.class, "HASHING_LOG2_ROUNDS", 0);
		Whitebox.setInternalState(SecureHashing.class, "SALT_SIZE", 0);
		String hash = SecureHashing.hashString(text);
		String expectedHash = "8cafed2235386cc5855e75f0d34f103ccc183912e5f02446b77c66539f776e4bf2bf87339b4518a7cb1c2441c568b0f8";
		assertEquals(expectedHash, hash);
	}
	
	/**
	 * Test {@link SecureHashing#hashString(String)} method.
	 * Scenario: result must be in lowercase hex dump format
	 */
	@Test
	public void /*static String*/ hashStringFormat(/*String text*/) {
		//PowerMockito.mockStatic(SecureHashing.class); // mocks all static methods of the class
		String text = "12345678";
		String hash = SecureHashing.hashString(text);
		assertTrue(String.format("Result \"%s\" must be in lowercase hex dump", hash), hash.matches("^[0123456789abcdef]+$"));
	}

	/**
	 * Test {@link SecureHashing#hashString(String)} method.
	 * Scenario: result must have proper size
	 */
	@Test
	public void /*static* String*/ hashStringSize(/*String text*/) {
		//PowerMockito.mockStatic(SecureHashing.class); // mocks all static methods of the class
		String text = "12345678";
		String hash = SecureHashing.hashString(text);
		assertEquals(String.format("Size of the generated %s hash text is invalid.", hash), SecureHashing.getHashLength(), hash.length());
	}

	/**
	 * Test {@link SecureHashing#hashString(String)} method.
	 * Scenario: running twice the results must be different because of random salt usage
	 */
	@Test
	public void /*static String*/ hashStringIndetermenistic(/*String text*/) {
		//PowerMockito.mockStatic(SecureHashing.class); // mocks all static methods of the class
		String text = "12345678";
		String hash0 = SecureHashing.hashString(text);
		String hash1 = SecureHashing.hashString(text);
		
		assertFalse("Running twice the results should be different because of random salt usage during hashing", hash0.equals(hash1));
	}

	/**
	* Test {@link SecureHashing#hashString(String, String)} method.
	* Scenario: creates hash successfully in 2^^0 round.
	*/
	@Test
	public void /*static String*/ hashStringDuo(/*String text, String salt*/) {
		String text = "12345678";
		String salt = "d883eb377dc74565e406c4ce1899c6e6";
		Whitebox.setInternalState(SecureHashing.class, "HASHING_LOG2_ROUNDS", 0);
		String hash = SecureHashing.hashString(text, salt);
		String expectedHash = salt + "eb34787296648963a193bcddc98b5352a9dcd12e4b30463cfa749be9508562fc430576d3f6a3752552bdf2e9c849a39e";
		assertEquals("Generated hash is not correct.", expectedHash, hash);
	}

	/**
	 * 
	 * Test {@link SecureHashing#hashString(String, String, int)} method.
	 * Scenario: creates hash successfully.
	 */
	@Test
	public void /*static String*/ hashStringTrio(/*String text, String salt, int log2Rounds*/) {
		String text = "12345678";
		String salt = "d883eb377dc74565e406c4ce1899c6e6";
		int log2Rounds = 10;
		String hash = SecureHashing.hashString(text, salt, log2Rounds);
		assertEquals(String.format("Size of the generated %s hash text is invalid.", hash), SecureHashing.getHashLength(), hash.length());
		String expectedHash = salt + "794182a2424c25d9a69d60ed5cb02d1109271c070554d49c551d2ac4c771beee6592f3701f29bc50eff676f9338b40f9";
		assertEquals("Generated hash is not correct.", expectedHash, hash);
	}

	/**
	 * Test {@link SecureHashing#hashString(String, String, int)} method.
	 * Scenario: fails with exception because input text is null
	 */
	@Test(expected=NullPointerException.class)
	public void /*static String*/ hashStringTrioNull(/*String text, String salt, int log2Rounds*/) {
		String text = null;
		String salt = "d883eb377dc74565e406c4ce1899c6e6";
		int log2Rounds = 10;
		/*String hash =*/ SecureHashing.hashString(text, salt, log2Rounds);
	}

	/**
	 * Test {@link SecureHashing#hashString(String, String, int)} method.
	 * Scenario: fails with exception because input log2Rounds is negative value
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*static String*/ hashStringTrioNegative(/*String text, String salt, int log2Rounds*/) {
		String text = "12345678";
		String salt = "d883eb377dc74565e406c4ce1899c6e6";
		int log2Rounds = -1;
		/*String hash =*/ SecureHashing.hashString(text, salt, log2Rounds);
	}

	/**
	 * Test {@link SecureHashing#validateHash(String, String)} method.
	 * Scenario: successfully validates input values.
	 */
	@Test
	public void /*static boolean*/ validateHash(/*String text, String hash*/) {
		String text = "12345678";
		String salt = "d883eb377dc74565e406c4ce1899c6e6";
		String hash = salt + "794182a2424c25d9a69d60ed5cb02d1109271c070554d49c551d2ac4c771beee6592f3701f29bc50eff676f9338b40f9";
		boolean isValidHash = SecureHashing.validateHash(text, hash);
		assertTrue(isValidHash);
	}
	
	/**
	 * Test {@link SecureHashing#validateHash(String, String)} method.
	 * Scenario: fails with exception because input hash has unexpected size.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*static boolean*/ validateHashInvalidCorrectHash(/*String text, String hash*/) {
		String text = "12345678";
		String hash = "1234";
		/*boolean isValidHash =*/ SecureHashing.validateHash(text, hash);
	}
	
	/**
	 * Test {@link SecureHashing#validateHash(String, String, int)} method.
	 */
	@Test
	public void /*static boolean*/ validateHashTrio(/*String text, String hash, int log_rounds*/) {
		String text = "12345678";
		String salt = "d883eb377dc74565e406c4ce1899c6e6";
		String hash = salt + "eb34787296648963a193bcddc98b5352a9dcd12e4b30463cfa749be9508562fc430576d3f6a3752552bdf2e9c849a39e";
		int log2Rounds = 0;
		boolean isValidHash = SecureHashing.validateHash(text, hash, log2Rounds);
		assertTrue(isValidHash);
	}
	
	/**
	 * Test {@link SecureHashing#generateSalt()} method.
	 * Scenario: successfully generates salt in proper size
	 */
	@Test
	public void /*static String*/ generateSalt() {
		String salt = SecureHashing.generateSalt();
		assertEquals(String.format("Size of the generated %s salt is invalid.", salt), SecureHashing.SALT_SIZE / 4, salt.length());
	}
	
	/**
	 * Test {@link SecureHashing#getHashLength()} method.
	 * Scenario: 
	 */
	@Test
	public void /*static* int*/ getHashLength() {
		int hashLength = SecureHashing.getHashLength();
		int expectedHashLength = 128;
		assertEquals("Expected hash size is different from the calculated one.", expectedHashLength, hashLength);
	}

}
