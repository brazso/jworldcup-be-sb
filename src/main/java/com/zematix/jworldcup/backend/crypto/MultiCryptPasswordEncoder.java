
package com.zematix.jworldcup.backend.crypto;

import java.security.SecureRandom;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.zematix.jworldcup.backend.crypto.SecureHashing.HashAlgorithm;

import lombok.NonNull;

/**
 * {@link PasswordEncoder} implementation of the versatile {@link SecureHashing} crypto class.
 */
public class MultiCryptPasswordEncoder implements PasswordEncoder {

	private final SecureHashing secureHashing;

	public MultiCryptPasswordEncoder() {
		this(null, null, null, null);
	}

	public MultiCryptPasswordEncoder(SecureRandom random) {
		this(null, null, null, random);
	}


	public MultiCryptPasswordEncoder(HashAlgorithm hashAlgorithm, Integer saltSize,
			Integer hashingLog2Rounds, SecureRandom random) {
		super();
		
		this.secureHashing = new SecureHashing(hashAlgorithm, saltSize, hashingLog2Rounds, random);
	}

	@Override
	public String encode(@NonNull CharSequence rawPassword) {
		return secureHashing.hashString(rawPassword.toString());
	}

	@Override
	public boolean matches(@NonNull CharSequence rawPassword, @NonNull String encodedPassword) {
		return secureHashing.validateHash(rawPassword.toString(), encodedPassword);
	}

	@Override
	public boolean upgradeEncoding(@NonNull String encodedPassword) {
		return secureHashing.getHashLength() != encodedPassword.length();
	}
	
}
