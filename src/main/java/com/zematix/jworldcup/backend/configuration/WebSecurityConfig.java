package com.zematix.jworldcup.backend.configuration;

import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import javax.servlet.http.HttpServletResponse;

import com.zematix.jworldcup.backend.crypto.MultiCryptPasswordEncoder;

/**
 * This class extends the WebSecurityConfigurerAdapter which is a convenience class that 
 * allows customization to both WebSecurity and HttpSecurity.
 */
@Profile("development")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true/*, securedEnabled = true, jsr250Enabled = true*/)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Inject
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Inject
	private UserDetailsService jwtUserDetailsService;

	@Inject
	private JwtRequestFilter jwtRequestFilter;
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new MultiCryptPasswordEncoder();
	}

	protected static final String[] ACTUATOR_WHITELIST = { "/login", "/signup", "/backend-version" };

	protected static final String[] SWAGGER_WHITELIST = { "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", };

	@Inject
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		// configure AuthenticationManager so that it knows from where to load
		// user for matching credentials. Use own MultiCryptPasswordEncoder.
		auth.userDetailsService(jwtUserDetailsService).passwordEncoder(passwordEncoder());
		// caching userDetails returned by loadUserByUsername method needs eraseCredentials off 
		// otherwise after each successful authentication its password field is erased
//		auth.eraseCredentials(false);
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		
		// We don't need CSRF for this example
		httpSecurity.csrf().disable()
				// dont authenticate this particular request
				.authorizeRequests()
				//.logout().permitAll().logoutSuccessHandler((request, response, authentication) -> {
                //    response.setStatus(HttpServletResponse.SC_OK);
                //})
				.antMatchers(Stream.concat(Stream.of(ACTUATOR_WHITELIST), Stream.of(SWAGGER_WHITELIST)).toArray(String[]::new)).permitAll()
		        //.antMatchers("/", "/**").permitAll()
				//.antMatchers(HttpMethod.OPTIONS, "/**").permitAll() // browser (angular) uses
				// all other requests need to be authenticated
				.anyRequest().authenticated()
				.and().
				// make sure we use stateless session; session won't be used to store user's state.
				exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and().sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		// Add a filter to validate the tokens with every request
		httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
		
	    httpSecurity.cors().configurationSource(request -> {
	        CorsConfiguration cors = new CorsConfiguration();
	        cors.setAllowedOrigins(List.of("http://localhost:4200", "http://127.0.0.1:80", "http://example.com"));
			cors.setAllowedMethods(List.of(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(),
					HttpMethod.DELETE.name())); // HttpMethod.DELETE.name() needed? 
//			cors.setAllowedHeaders(List.of("*"));
			cors.setAllowCredentials(true);
	        cors.applyPermitDefaultValues();
	        return cors;
	    });
	}

}
