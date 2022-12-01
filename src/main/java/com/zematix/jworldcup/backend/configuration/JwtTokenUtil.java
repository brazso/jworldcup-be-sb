package com.zematix.jworldcup.backend.configuration;

import java.io.Serializable;
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
	
	@Value("${jwt.validity.access:600}") // default is 10 minutes in seconds
	private String jwtValidityAccess;
	
	@Value("${jwt.validity.refresh:86400}") // default is 24 hours in seconds
	private String jwtValidityRefresh;

	@Value("${app.shortName}")
	private String appShortName;

	//retrieve username from jwt token
	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	//retrieve expiration date from jwt token
	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}
	
    //for retrieveing any information from token we will need the secret key
	private Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
	}

	//check if the token has expired
	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	//generate access token for user
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

	//generate refresh token for user
	private String generateRefreshToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		
		claims.put(Claims.ISSUER, appShortName);
		
//		final String authorities = userDetails.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.joining(","));
//		claims.put(AUTHORITIES_KEY, authorities);
		
		claims.put("tokenType", "REFRESH");
		
		return doGenerateToken(claims, userDetails.getUsername(), jwtValidityRefresh);
	}

	//generate refresh token for user
	public ResponseCookie generateRefreshTokenCookie(UserDetails userDetails) {
		String refreshToken = generateRefreshToken(userDetails);
		
		return ResponseCookie.from("refreshToken", refreshToken)
				.maxAge(Long.parseLong(jwtValidityRefresh))
				.httpOnly(true)
				.secure(true)
				.sameSite("None")
				.build();
	}

	//while creating the token -
	//1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
	//2. Sign the JWT using the HS512 algorithm and secret key.
	//3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
	//   compaction of the JWT to a URL-safe string 
	private String doGenerateToken(Map<String, Object> claims, String subject, String jwtValidity) {

		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(jwtValidity) * 1000))
				.signWith(SignatureAlgorithm.HS512, jwtSecret).compact();
	}

	//validate token
	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = getUsernameFromToken(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}
}