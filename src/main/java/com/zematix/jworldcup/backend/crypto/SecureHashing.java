package com.zematix.jworldcup.backend.crypto;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.math.LongMath.pow;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import lombok.NonNull;

/**
 * A utility class for creating secure hashes using different hashing 
 * algorithms as well as validating the generated hashes with the original
 * value. The generated hash contains a random salt value in its prefix.
 * Both the generated hash size and the iteration numbers of the 
 * hashing execution times can be given.
 * 
 * @see http://crackstation.net/hashing-security.html
 * @see http://www.jasypt.org/howtoencryptuserpasswords.html
 * @see https://www.mindrot.org/projects/jBCrypt/
 */
public class SecureHashing {
	
	/**
	 * Supported hash algorithm variances
	 */
	public enum HashAlgorithm {
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
	
	/**
	 * Used default hash algorithm.
	 */
	public static final HashAlgorithm HASH_ALGORITHM = HashAlgorithm.SHA384;
	
	/**
	 * Default salt size in bits.
	 */
	public static final Integer SALT_SIZE = 128;
	
	/**
	 * Default 2 powered by this value execution times of the hashing algorithm. 
	 */
	public static final Integer HASHING_LOG2_ROUNDS = 10;

	/**
	 * Mimimum value of {@link SecureHashing#HASHING_LOG2_ROUNDS. 2 powered by 0
	 * means exactly 1 hashing execution takes place.
	 */
	public static final Integer MIN_HASHING_LOG2_ROUNDS = 0;

	/**
	 * Maximum value of {@link SecureHashing#HASHING_LOG2_ROUNDS. 2 powered by 63 cannot be
	 * stored in a {@code long} variable, that is why there is this restriction. Its value
	 * cannot be more than 62. 
	 */
	public static final Integer MAX_HASHING_LOG2_ROUNDS = 62;

	/**
	 * Validate static constants during class loading just for the sake of certainty
	 */
	static {
		checkNotNull(HASH_ALGORITHM);
		checkNotNull(SALT_SIZE);
		checkNotNull(HASHING_LOG2_ROUNDS);
		checkArgument(SALT_SIZE % 4 == 0 && SALT_SIZE >= 0, "SALT_SIZE must be divisible by 4 and must be non negative");
		checkArgument(HASHING_LOG2_ROUNDS >= MIN_HASHING_LOG2_ROUNDS && HASHING_LOG2_ROUNDS <= MAX_HASHING_LOG2_ROUNDS, String.format("HASHING_LOG2_ROUNDS must be between %d and %d", MIN_HASHING_LOG2_ROUNDS, MAX_HASHING_LOG2_ROUNDS));
	}

	/**
	 * Used hash algorithm.
	 */
	private final HashAlgorithm hashAlgorithm;
	
	/**
	 * Salt size in bits. The generated salt is a hex dump, so its value must be divisible by 4.
	 * If 0 is given, salt is not used.
	 * Primitive int type cannot be mocked during test, that is why got Integer type.
	 * Fixed value but it could be variable.
	 */
	private final Integer saltSize;
	
	/**
	 * Slowing brute force attack of the hashed values, the hashing algorithm runs in
	 * 2 powered by this value times. 
	 * Its value can be between {@link SecureHashing#MIN_HASHING_LOG2_ROUNDS} and 
	 * {@link SecureHashing#MAX_HASHING_LOG2_ROUNDS}. 
	 * This value can be also altered in overloaded 
	 * {@link SecureHashing#hashString(String, String, int)} method.
	 */
	public final Integer hashingLog2Rounds;

	/**
	 * Provides a cryptographically strong random number generator (RNG) object. 
	 */
	private final SecureRandom random;

	public SecureHashing() {
		this(null, null, null, null);
	}

	public SecureHashing(SecureRandom random) {
		this(null, null, null, random);
	}

	public SecureHashing(HashAlgorithm hashAlgorithm, Integer saltSize,
			Integer hashingLog2Rounds, SecureRandom random) {
		super();
		this.hashAlgorithm = MoreObjects.firstNonNull(hashAlgorithm, HASH_ALGORITHM);
		this.saltSize = MoreObjects.firstNonNull(saltSize, SALT_SIZE);
		this.hashingLog2Rounds = MoreObjects.firstNonNull(hashingLog2Rounds, HASHING_LOG2_ROUNDS);
		this.random = MoreObjects.firstNonNull(random, new SecureRandom()); // generate RNG object unless is null
	}

	/**
	 * Generates hash from random salt value concatenated by input {@code text}. 
	 * The salt is stored in the result as prefix.
	 * @param text
	 * @return generated hash value
	 */
	public String hashString(@NonNull String text) {
		String salt = generateSalt();
		return hashString(text, salt, this.hashingLog2Rounds);
	}

	/**
	 * Generates hash from input {@code salt} value concatenated by input {@code text}. 
	 * The {@code salt} is stored in the result as prefix.
	 * @param text
	 * @param salt
	 * @return generated hash value
	 */
	public String hashString(@NonNull String text, @NonNull String salt) {
		return hashString(text, salt, this.hashingLog2Rounds);
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
	public String hashString(@NonNull String text, @NonNull String salt, int log2Rounds) {
		checkArgument(salt.length() % 4 == 0 && salt.length() >= 0, "salt length must be non-negative and divisible by 4");
		checkArgument(log2Rounds >= MIN_HASHING_LOG2_ROUNDS && log2Rounds <= MAX_HASHING_LOG2_ROUNDS, String.format("Argument \"log2Rounds\" must be between %d and %d", MIN_HASHING_LOG2_ROUNDS, MAX_HASHING_LOG2_ROUNDS));
		HashFunction func = this.hashAlgorithm.getHashFunction();
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
	public boolean validateHash(@NonNull String text, @NonNull String hash) {
		return validateHash(text, hash, this.hashingLog2Rounds);
	}

	/**
	 * Matches the given {@code text} value with the given {@code hash} value. 
	 * If hashing the {@code text} value results the given {@code hash} it returns {@code true}.
	 * @param text
	 * @param hash
	 * @param log_rounds
	 * @return
	 */
	public boolean validateHash(@NonNull String text, @NonNull String hash, int logRounds) {
		checkArgument(hash.length() == getHashLength(), String.format("Argument hash must be %d in size", getHashLength()));
		String salt = hash.substring(0, this.saltSize / 4);
		return hashString(text, salt, logRounds).equals(hash);
	}

	/**
	 * Generates random salt text value containing hexadecimal numbers in lowercase.
	 * The length of the generated salt is {@link SecureHashing#SALT_SIZE} / 4. 
	 * @return generated salt value
	 */
	public String generateSalt() {
		if (this.saltSize == 0) {
			return "";
		}
		String randomHexString = new BigInteger(this.saltSize, this.random).toString(16);
		return Strings.padStart(randomHexString, this.saltSize / 4, '0');
	}
	
	/**
	 * Returns the result hash size in characters. 
	 * @return hash size in characters
	 */
	public int getHashLength() {
		return (this.saltSize + this.hashAlgorithm.getSize()) / 4;
	}
}
