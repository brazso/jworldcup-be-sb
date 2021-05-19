package com.zematix.jworldcup.backend.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import com.zematix.jworldcup.backend.crypto.SecureHashing;
import com.zematix.jworldcup.backend.crypto.SecureHashing.HashAlgorithm;

/**
 * Contains test functions of {@link SecureHashing} class.
 * All hashing functions use only {@link HashAlgorithm#SHA384} hashing algorithm.
 * 
 *  @see https://hash.online-convert.com/sha384-generator
 */
@SpringBootTest
public class SecureHashingTest {

	private SecureHashing secureHashing; // object to be tested
	
	/**
	 * Initializes constants used in our tests
	 */
	@Before
	public void initialize() {
		secureHashing = new SecureHashing();
	}
	
	/**
	 * Test {@link SecureHashing#hashString(String)} method.
	 * Scenario: creates hash successfully using fix salt. 
	 */
	@Test
	public void /*String*/ hashString(/*String text*/) {
		secureHashing = Mockito.spy(secureHashing); // partial mock - mocks only some methods of the class
		String text = "12345678";
		String salt = "d883eb377dc74565e406c4ce1899c6e6";
		Mockito.when(secureHashing.generateSalt()).thenReturn(salt);
		String hash = secureHashing.hashString(text);
		String expectedHash = salt + "794182a2424c25d9a69d60ed5cb02d1109271c070554d49c551d2ac4c771beee6592f3701f29bc50eff676f9338b40f9";
		assertEquals(expectedHash, hash);
	}

	/**
	 * Test {@link SecureHashing#hashString(String)} method.
	 * Scenario: creates hash successfully using fix salt in 2^^0 round. 
	 */
	@Test
	public void /*String*/ hashStringOnce(/*String text*/) {
		secureHashing = new SecureHashing(null, null, /*hashingLog2Rounds*/ 0, null);
		secureHashing = Mockito.spy(secureHashing); // partial mock
		String text = "12345678";
		String salt = "d883eb377dc74565e406c4ce1899c6e6";
		Mockito.when(secureHashing.generateSalt()).thenReturn(salt);
		String hash = secureHashing.hashString(text);
		String expectedHash = salt + "eb34787296648963a193bcddc98b5352a9dcd12e4b30463cfa749be9508562fc430576d3f6a3752552bdf2e9c849a39e";
		assertEquals(expectedHash, hash);
	}
	
	/**
	 * Test {@link SecureHashing#hashString(String)} method.
	 * Scenario: creates hash successfully without salt in 2^^0 round. 
	 */
	@Test
	public void /*String*/ hashStringOnceWoSalt(/*String text*/) {
		String text = "12345678";
		secureHashing = new SecureHashing(null, /*saltSize*/ 0, /*hashingLog2Rounds*/ 0, null);
		String hash = secureHashing.hashString(text);
		String expectedHash = "8cafed2235386cc5855e75f0d34f103ccc183912e5f02446b77c66539f776e4bf2bf87339b4518a7cb1c2441c568b0f8";
		assertEquals(expectedHash, hash);
	}
	
	/**
	 * Test {@link SecureHashing#hashString(String)} method.
	 * Scenario: result must be in lowercase hex dump format
	 */
	@Test
	public void /*String*/ hashStringFormat(/*String text*/) {
		String text = "12345678";
		String hash = secureHashing.hashString(text);
		assertTrue(String.format("Result \"%s\" must be in lowercase hex dump", hash), hash.matches("^[0123456789abcdef]+$"));
	}

	/**
	 * Test {@link SecureHashing#hashString(String)} method.
	 * Scenario: result must have proper size
	 */
	@Test
	public void /*String*/ hashStringSize(/*String text*/) {
		String text = "12345678";
		String hash = secureHashing.hashString(text);
		assertEquals(String.format("Size of the generated %s hash text is invalid.", hash), secureHashing.getHashLength(), hash.length());
	}

	/**
	 * Test {@link SecureHashing#hashString(String)} method.
	 * Scenario: running twice the results must be different because of random salt usage
	 */
	@Test
	public void /*String*/ hashStringIndetermenistic(/*String text*/) {
		String text = "12345678";
		String hash0 = secureHashing.hashString(text);
		String hash1 = secureHashing.hashString(text);
		
		assertNotEquals("Running twice the results should be different because of random salt usage during hashing", hash0, hash1);
	}

	/**
	* Test {@link SecureHashing#hashString(String, String)} method.
	* Scenario: creates hash successfully in 2^^0 round.
	*/
	@Test
	public void /*String*/ hashStringDuo(/*String text, String salt*/) {
		String text = "12345678";
		String salt = "d883eb377dc74565e406c4ce1899c6e6";
		secureHashing = new SecureHashing(null, null, /*hashingLog2Rounds*/ 0, null);
		String hash = secureHashing.hashString(text, salt);
		String expectedHash = salt + "eb34787296648963a193bcddc98b5352a9dcd12e4b30463cfa749be9508562fc430576d3f6a3752552bdf2e9c849a39e";
		assertEquals("Generated hash is not correct.", expectedHash, hash);
	}

	/**
	 * 
	 * Test {@link SecureHashing#hashString(String, String, int)} method.
	 * Scenario: creates hash successfully.
	 */
	@Test
	public void /*String*/ hashStringTrio(/*String text, String salt, int log2Rounds*/) {
		String text = "12345678";
		String salt = "d883eb377dc74565e406c4ce1899c6e6";
		int log2Rounds = 10;
		String hash = secureHashing.hashString(text, salt, log2Rounds);
		assertEquals(String.format("Size of the generated %s hash text is invalid.", hash), secureHashing.getHashLength(), hash.length());
		String expectedHash = salt + "794182a2424c25d9a69d60ed5cb02d1109271c070554d49c551d2ac4c771beee6592f3701f29bc50eff676f9338b40f9";
		assertEquals("Generated hash is not correct.", expectedHash, hash);
	}

	/**
	 * Test {@link SecureHashing#hashString(String, String, int)} method.
	 * Scenario: fails with exception because input text is null
	 */
	@Test(expected=NullPointerException.class)
	public void /*String*/ hashStringTrioNull(/*String text, String salt, int log2Rounds*/) {
		String text = null;
		String salt = "d883eb377dc74565e406c4ce1899c6e6";
		int log2Rounds = 10;
		secureHashing.hashString(text, salt, log2Rounds);
	}

	/**
	 * Test {@link SecureHashing#hashString(String, String, int)} method.
	 * Scenario: fails with exception because input log2Rounds is negative value
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*String*/ hashStringTrioNegative(/*String text, String salt, int log2Rounds*/) {
		String text = "12345678";
		String salt = "d883eb377dc74565e406c4ce1899c6e6";
		int log2Rounds = -1;
		secureHashing.hashString(text, salt, log2Rounds);
	}

	/**
	 * Test {@link SecureHashing#validateHash(String, String)} method.
	 * Scenario: successfully validates input values.
	 */
	@Test
	public void /*boolean*/ validateHash(/*String text, String hash*/) {
		String text = "12345678";
		String salt = "d883eb377dc74565e406c4ce1899c6e6";
		String hash = salt + "794182a2424c25d9a69d60ed5cb02d1109271c070554d49c551d2ac4c771beee6592f3701f29bc50eff676f9338b40f9";
		boolean isValidHash = secureHashing.validateHash(text, hash);
		assertTrue(isValidHash);
	}
	
	/**
	 * Test {@link SecureHashing#validateHash(String, String)} method.
	 * Scenario: fails with exception because input hash has unexpected size.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*boolean*/ validateHashInvalidCorrectHash(/*String text, String hash*/) {
		String text = "12345678";
		String hash = "1234";
		secureHashing.validateHash(text, hash);
	}
	
	/**
	 * Test {@link SecureHashing#validateHash(String, String, int)} method.
	 */
	@Test
	public void /*boolean*/ validateHashTrio(/*String text, String hash, int log_rounds*/) {
		String text = "12345678";
		String salt = "d883eb377dc74565e406c4ce1899c6e6";
		String hash = salt + "eb34787296648963a193bcddc98b5352a9dcd12e4b30463cfa749be9508562fc430576d3f6a3752552bdf2e9c849a39e";
		int log2Rounds = 0;
		boolean isValidHash = secureHashing.validateHash(text, hash, log2Rounds);
		assertTrue(isValidHash);
	}
	
	/**
	 * Test {@link SecureHashing#generateSalt()} method.
	 * Scenario: successfully generates salt in proper size
	 */
	@Test
	public void /*String*/ generateSalt() {
		String salt = secureHashing.generateSalt();
		assertEquals(String.format("Size of the generated %s salt is invalid.", salt), SecureHashing.SALT_SIZE / 4, salt.length());
	}
	
	/**
	 * Test {@link SecureHashing#getHashLength()} method.
	 * Scenario: 
	 */
	@Test
	public void /*int*/ getHashLength() {
		int hashLength = secureHashing.getHashLength();
		int expectedHashLength = 128;
		assertEquals("Expected hash size is different from the calculated one.", expectedHashLength, hashLength);
	}

}
