package com.paymybuddy.paymybuddy.configuration;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.AllArgsConstructor;

/**
 * Spring Security Configuration
 * @author Olivier MOREL
 *
 */
@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfiguration  {
	
	private final PasswordEncoder passwordEncoder;
	
	private final UserDetailsService userDetailsService;
	
	@Bean
	public AuthenticationManager authenticationManager() {
	    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
	    provider.setPasswordEncoder(passwordEncoder);
	    provider.setUserDetailsService(userDetailsService);
	    return new ProviderManager(provider);
	}
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(authz -> authz
					.requestMatchers("/","/register", "/createregistered").permitAll()
					.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
					.requestMatchers("/user/**").hasRole("USER")
					.requestMatchers("/admin/*").hasRole("ADMIN")
					.anyRequest().authenticated())
			.formLogin(form -> form
					.loginPage("/login")
					.defaultSuccessUrl("/user/home", true)
					.permitAll())
			.rememberMe(conf -> conf
					.rememberMeParameter("remember")
					.rememberMeCookieName("rememberlogin")
					.tokenValiditySeconds(300))
			.logout(logout -> logout.permitAll());
		return http.build();
	}
}
	

