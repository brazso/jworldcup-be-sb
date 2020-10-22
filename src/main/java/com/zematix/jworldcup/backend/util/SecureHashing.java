package com.zematix.jworldcup.backend.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.math.LongMath.pow;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * A utility class for creating secure hashes using different hashing 
 * algorithms as well as validating the generated hashes with the original
 * value. The hash input text is preappended by a random salt value. The
 * generated hash contains this salt value as its prefix.
 * Both the generated hash size and the iteration numbers of the 
 * hashing execution times can be given.
 * 
 * @see http://crackstation.net/hashing-security.html
 * @see http://www.jasypt.org/howtoencryptuserpasswords.html
 * @see https://www.mindrot.org/projects/jBCrypt/
 */
public final class SecureHashing {
	
	/**
	 * Supported hash algorithm variances
	 */
	@VisibleForTesting
	/*public*/ enum HashAlgorithm {
		@Deprecated
		MD5(128, Hashing.md5()),
		@Deprecated
		SHA1(160, Hashing.sha1()),
		SHA256(256, Hashing.sha256()),
		SHA384(384, Hashing.sha384()),
		SHA512(512, Hashing.sha512());

		private int size;
		private HashFunction hashFunction;

		private HashAlgorithm(int size, HashFunction hashFunction) {
			this.size = size;
			this.hashFunction = hashFunction;
		}
		
		public int getSize() {
			return this.size;
		}
		
		public HashFunction getHashFunction() {
			return this.hashFunction;
		}
	}
	
	private static final SecureRandom RANDOM = new SecureRandom();
	
	/**
	 * Used hash algorithm. Fixed value.
	 */
	public static final HashAlgorithm HASH_ALGORITHM = HashAlgorithm.SHA384;
	
	/**
	 * Salt size in bits. The generated salt is a hex dump, so its value must be divisible by 4.
	 * If 0 is given, salt is not used.
	 * Primitive int type cannot be mocked during test, that is why got Integer type.
	 * Fixed value.
	 */
	public static final Integer SALT_SIZE = 128;
	
	/**
	 * Slowing brute force attack of the hashed values, the hashing algorithm runs in
	 * 2 powered by this value times. 
	 * Its value can be between 0 and {@link SecureHashing#MAX_HASHING_LOG2_ROUNDS} . 
	 * Seems fixed value but its value can be altered in 
	 * overloaded {@link SecureHashing#hashString(String, String, int)} method.
	 */
	public static final Integer HASHING_LOG2_ROUNDS = 10;

	/**
	 * Maximum value of {@link SecureHashing#HASHING_LOG2_ROUNDS. 2 powered by 63 cannot be
	 * stored in a {@code long} variable, that is why there is this restriction. 
	 * Fixed value. Do not modify this value!
	 */
	public static final Integer MAX_HASHING_LOG2_ROUNDS = 62;

	/**
	 * Validate constants during class loading
	 */
	static {
		checkNotNull(HASH_ALGORITHM);
		checkNotNull(SALT_SIZE);
		checkNotNull(HASHING_LOG2_ROUNDS);
		checkArgument(SALT_SIZE % 4 == 0 && SALT_SIZE >= 0, "SALT_SIZE must be divisible by 4 and must be non negative");
		checkArgument(HASHING_LOG2_ROUNDS >= 0 && HASHING_LOG2_ROUNDS <= MAX_HASHING_LOG2_ROUNDS, String.format("HASHING_LOG2_ROUNDS must be between 0 and %d", MAX_HASHING_LOG2_ROUNDS));
	}
	
	/**
	 * Implicit construction is disallowed.
	 */
	private SecureHashing() {
		throw new AssertionError();
	}

	/**
	 * Generates hash from random salt value concatenated by input {@code text}. 
	 * The salt is stored in the result as prefix.
	 * @param text
	 * @return generated hash value
	 */
	public static String hashString(String text) {
		String salt = generateSalt();
		return hashString(text, salt, HASHING_LOG2_ROUNDS);
	}

	/**
	 * Generates hash from input {@code salt} value concatenated by input {@code text}. 
	 * The {@code salt} is stored in the result as prefix.
	 * @param text
	 * @param salt
	 * @return generated hash value
	 */
	public static String hashString(String text, String salt) {
		return hashString(text, salt, HASHING_LOG2_ROUNDS);
	}

	/**
	 * Generates hash from input {@code salt} value concatenated by input {@code text}.
	 * Hash algorithms is executed 2^^{@code log2Rounds} times.
	 * The {@code salt} is stored in the result as prefix.
	 * @param text
	 * @param salt
	 * @param log2_rounds
	 * @return generated hash value 
	 */
	public static String hashString(String text, String salt, int log2Rounds) {
		checkNotNull(text, "Argument \"text\" must not be null");
		checkNotNull(salt, "Argument \"salt\" must not be null");
		checkArgument(salt.length() % 4 == 0 && salt.length() >= 0, "salt length be divisible by 4");
		checkArgument(log2Rounds >= 0 && log2Rounds <= MAX_HASHING_LOG2_ROUNDS, String.format("Argument \"log2Rounds\" must be between 0 and %d", MAX_HASHING_LOG2_ROUNDS));
		HashFunction func = HASH_ALGORITHM.getHashFunction();
		HashCode result = func.hashString(salt + text, StandardCharsets.UTF_8);
		for (long l = 0; l < pow(2, log2Rounds)-1; l++) {
			result = func.hashBytes(result.asBytes());
		}
		return salt + result.toString();
	}
	
	/**
	 * Matches the given {@code text} value with the given {@code hash} value. 
	 * If hashing the {@code text} value results the given {@code hash} it returns {@code true}.
	 * @param text
	 * @param hash
	 * @return {@code true} if hashing text is equal to hash.
	 */
	public static boolean validateHash(String text, String hash) {
		return validateHash(text, hash, HASHING_LOG2_ROUNDS);
	}

	/**
	 * Matches the given {@code text} value with the given {@code hash} value. 
	 * If hashing the {@code text} value results the given {@code hash} it returns {@code true}.
	 * @param text
	 * @param hash
	 * @param log_rounds
	 * @return
	 */
	public static boolean validateHash(String text, String hash, int logRounds) {
		checkNotNull(text, "Argument \"text\" must not be null");
		checkNotNull(hash, "Argument \"hash\" must not be null");
		checkArgument(hash.length() == getHashLength(), String.format("Argument hash must be %d in size", getHashLength()));
		String salt = hash.substring(0, SALT_SIZE / 4);
		return hashString(text, salt, logRounds).equals(hash);
	}

	/**
	 * Generates random salt text value containing hexadecimal numbers in lowercase.
	 * The length of the generated salt is {@link SecureHashing#SALT_SIZE} / 4. 
	 * @return generated salt value
	 */
	public static String generateSalt() {
		if (SALT_SIZE == 0) {
			return "";
		}
		String randomHexString = new BigInteger(SALT_SIZE, RANDOM).toString(16);
		return Strings.padStart(randomHexString, SALT_SIZE / 4, '0');
	}
	
	/**
	 * Returns the result hash size in characters. 
	 * @return hash size in characters
	 */
	public static int getHashLength() {
		return (SALT_SIZE + HASH_ALGORITHM.getSize()) / 4;
	}
}
