package com.zematix.jworldcup.backend.configuration;

import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer.SessionFixationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;

import com.zematix.jworldcup.backend.crypto.MultiCryptPasswordEncoder;
import com.zematix.jworldcup.backend.service.SessionLogoutHandler;

/**
 * This class extends the WebSecurityConfigurerAdapter which is a convenience class that 
 * allows customization to both WebSecurity and HttpSecurity.
 */
@Profile("develop")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityDevelopConfig {

	@Inject
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Inject
	private UserDetailsService jwtUserDetailsService;

	@Inject
	private JwtRequestFilter jwtRequestFilter;
	
	@Inject
	private SessionLogoutHandler logoutHandler;

//	@@Inject
//	private ChangeSessionIdAuthenticationStrategyEx sessionAuthenticationStrategy;
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return new MultiCryptPasswordEncoder();
	}

	@Bean
	SessionRegistry sessionRegistry() { 
	    return new SessionRegistryImpl(); 
	}
	
	protected static final String[] ACTUATOR_WHITELIST = { "/login", "/refresh", "/signup", "/backend-version",
			"/verify-captcha", "/users/reset-password", "/users/process-registration-token", 
			"/users/process-change-email-token", "/users/process-reset-password-token" };

	protected static final String[] SWAGGER_WHITELIST = { "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", };

	@Inject
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		// configure AuthenticationManager so that it knows from where to load
		// user for matching credentials. Use own MultiCryptPasswordEncoder.
		auth.userDetailsService(jwtUserDetailsService).passwordEncoder(passwordEncoder());
		// caching userDetails returned by loadUserByUsername method needs eraseCredentials off 
		// otherwise after each successful authentication its password field is erased
		auth.eraseCredentials(false);
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authConfiguration) throws Exception {
		return authConfiguration.getAuthenticationManager();
	}

	@Bean
	SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf(AbstractHttpConfigurer::disable)
				// don't authenticate these particular requests
				.authorizeHttpRequests(requests -> requests
						.antMatchers(Stream.concat(Stream.of(ACTUATOR_WHITELIST), Stream.of(SWAGGER_WHITELIST))
								.toArray(String[]::new))
						.permitAll()
						// but all other requests need to be authenticated
						.anyRequest().authenticated())
				.exceptionHandling(handling -> handling.authenticationEntryPoint(jwtAuthenticationEntryPoint))
				// make sure we use stateless session; session won't be used to store user's
				// state.
				.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		httpSecurity.logout(logout -> logout.addLogoutHandler(logoutHandler)
				.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)).permitAll());

		// Add a filter to validate the tokens with every request
		httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

		httpSecurity.cors(cors -> cors.configurationSource(request -> {
			CorsConfiguration corsConfig = new CorsConfiguration();
			corsConfig.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:8080"));
			corsConfig.setAllowedMethods(List.of(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(),
					HttpMethod.DELETE.name()));
			// corsConfig.setAllowedHeaders(List.of("*"));
			corsConfig.setAllowCredentials(true);
			corsConfig.applyPermitDefaultValues();
			return corsConfig;
		}));

//      httpSecurity.sessionManagement(management -> management.sessionAuthenticationStrategy(sessionAuthenticationStrategy)
//				.maximumSessions(-1) // concurrent REST API calls from the same authenticated user might need more sessions
//				.sessionRegistry(sessionRegistry()));

		httpSecurity.sessionManagement(management -> management
				.sessionConcurrency(concurrency -> concurrency.maximumSessions(1).sessionRegistry(sessionRegistry()))
				// works flawlessly but session fixation invokes security risk
				.sessionFixation(SessionFixationConfigurer::none));

		return httpSecurity.build();
	}
}
