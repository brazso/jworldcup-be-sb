package com.zematix.jworldcup.backend.configuration;

import java.io.Serializable;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * The JwtTokenUtil is responsible for performing JWT operations like creation 
 * and validation.It makes use of the io.jsonwebtoken.Jwts for achieving this. 
 */
@Component
public class JwtTokenUtil implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String AUTHORITIES_KEY = "scopes";
	
	@Value("${jwt.secret}")
	private String jwtSecret;
	
	/**
	 * Access token expiration age in seconds
	 */
	@Value("${jwt.validity.access}")
	private String jwtValidityAccess;
	
	/**
	 * Refresh token expiration age in seconds
	 */
	@Value("${jwt.validity.refresh}")
	private String jwtValidityRefresh;

	@Value("${app.shortName}")
	private String appShortName;
	
	@Value("${server.servlet.context-path}")
	private String apiContextPath;

	/**
	 * Retrieves username from the given jwt token
	 * @param token
	 * @return username from the given jwt token
	 * @exception JwtException
	 */
	public String getUsernameFromToken(String token) throws JwtException {
		return getClaimFromToken(token, Claims::getSubject);
	}

	/**
	 * Retrieves expiration date from the given jwt token 
	 * @param token
	 * @return expiration date from the given jwt token
	 * @exception JwtException
	 */
	public Date getExpirationDateFromToken(String token) throws JwtException {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	/**
	 * Retrieves the given claim form the given jwt token 
	 * @param <T>
	 * @param token
	 * @param claimsResolver
	 * @return claim from jwt token
	 * @exception JwtException
	 */
	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) throws JwtException {
		Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}
	
    //For retrieving any information from token we will need the secret key. 
	//Used parseClaimsJws method may throw different jwt RuntimeExceptions which might be
	//checked from the caller.
	/**
	 * Retrieves all claims stored in the given jwt token.
	 * Used parseClaimsJws method may throw different jwt RuntimeExceptions which might be
	 * checked from the caller.
	 * @param token
	 * @return all claims stored in the given jwt token
	 * @exception JwtException
	 */
	private Claims getAllClaimsFromToken(String token) throws JwtException {
		return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
	}

	/**
	 * Generates access token for user of the given details. 
	 * @param userDetails
	 * @return access jwt token
	 */
	public String generateAccessToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		
		claims.put(Claims.ISSUER, appShortName);
		
		final String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
		claims.put(AUTHORITIES_KEY, authorities);
		
		claims.put("tokenType", "ACCESS");
		
		return doGenerateToken(claims, userDetails.getUsername(), jwtValidityAccess);
	}

	/**
	 * Generates refresh token for user of the given details. 
	 * @param userDetails
	 * @return refresh jwt token
	 */
	private String generateRefreshToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		
		claims.put(Claims.ISSUER, appShortName);
		claims.put("tokenType", "REFRESH");
		
		return doGenerateToken(claims, userDetails.getUsername(), jwtValidityRefresh);
	}

	/**
	 * Generates a response cookie containing a new refresh token for user of the given details. 
	 * @param userDetails
	 * @return refresh jwt token
	 */
	public ResponseCookie generateRefreshTokenCookie(UserDetails userDetails) {
		String refreshToken = generateRefreshToken(userDetails);
		
		return ResponseCookie.from("refreshToken", refreshToken)
				.path(apiContextPath)
				.maxAge(Long.parseLong(jwtValidityRefresh))
				.httpOnly(true)
				.secure(true)
				.sameSite("None")
				.build();
	}

	/**
	 * Generates a response cookie containing a new refresh token for user of the given details. 
	 * @param userDetails
	 * @return refresh jwt token
	 */
	public ResponseCookie generateDeletedRefreshTokenCookie() {
		return ResponseCookie.from("refreshToken", null)
				.path(apiContextPath)
				.maxAge(Duration.ofSeconds(0)) // Don't set to -1 or it will become a session cookie!
				.httpOnly(true)
				.secure(true)
				.sameSite("None")
				.build();
	}

	/**
	 * Generates a new jwt token from the given parameters.
	 * 1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
	 * 2. Sign the JWT using the HS512 algorithm and secret key.
	 * 3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
	 *    compaction of the JWT to a URL-safe string
	 * @param claims to be stored in the new token
	 * @param subject username
	 * @param jwtValidity expiration age in seconds
	 * @return new jwt token
	 */
	private String doGenerateToken(Map<String, Object> claims, String subject, String jwtValidity) {

		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(jwtValidity) * 1000))
				.signWith(SignatureAlgorithm.HS512, jwtSecret).compact();
	}
}