package com.zematix.jworldcup.backend.configuration;

import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
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
@EnableGlobalMethodSecurity(prePostEnabled = true) // enable @PreAuthorize and @PostAuthorize annotations for methods
public class WebSecurityDevelopConfig extends WebSecurityConfigurerAdapter {

	@Inject
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Inject
	private UserDetailsService jwtUserDetailsService;

	@Inject
	private JwtRequestFilter jwtRequestFilter;
	
	@Autowired
    private SessionLogoutHandler logoutHandler;
	
//	@Autowired
//	private ChangeSessionIdAuthenticationStrategyEx sessionAuthenticationStrategy;
	
	@Bean
	public PasswordEncoder passwordEncoder() {
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
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		
		httpSecurity.csrf().disable()
				// dont authenticate this particular request
				.authorizeRequests()
				.antMatchers(Stream.concat(Stream.of(ACTUATOR_WHITELIST), Stream.of(SWAGGER_WHITELIST))
						.toArray(String[]::new))
				.permitAll()
				// .antMatchers("/", "/**").permitAll()
				// .antMatchers(HttpMethod.OPTIONS, "/**").permitAll() // browser (angular) uses
				// all other requests need to be authenticated
				.anyRequest().authenticated().and().
				exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and().
				// make sure we use stateless session; session won't be used to store user's
				// state.
				sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		
		httpSecurity.logout()
				.addLogoutHandler(logoutHandler)
				.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
				.permitAll();
		
		// Add a filter to validate the tokens with every request
		httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
		
	    httpSecurity.cors().configurationSource(request -> {
	        CorsConfiguration cors = new CorsConfiguration();
	        cors.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:8080"));
			cors.setAllowedMethods(List.of(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(),
					HttpMethod.DELETE.name())); 
//			cors.setAllowedHeaders(List.of("*"));
			cors.setAllowCredentials(true);
	        cors.applyPermitDefaultValues();
	        return cors;
	    });

//		httpSecurity.sessionManagement().sessionAuthenticationStrategy(sessionAuthenticationStrategy)
//				.maximumSessions(-1) // concurrent REST API calls from the same authenticated user might need more sessions
//				.sessionRegistry(sessionRegistry());
		
		httpSecurity.sessionManagement()
				.maximumSessions(1)
				.sessionRegistry(sessionRegistry()).and().
				sessionFixation().none(); // works flawlessly but session fixation invokes security risk
	}

}
