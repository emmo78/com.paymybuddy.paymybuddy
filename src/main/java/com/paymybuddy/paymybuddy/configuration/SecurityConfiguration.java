package com.paymybuddy.paymybuddy.configuration;

import org.springframework.beans.factory.annotation.Autowired;
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

@Configuration
@EnableWebSecurity
public class SecurityConfiguration  {
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	UserDetailsService userDetailsService;
	
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
					.antMatchers("/").permitAll()
					.antMatchers("/user/*").hasRole("USER")
					.antMatchers("/admin/*").hasRole("ADMIN")
	                .anyRequest().authenticated())
			.formLogin(form -> form
					.loginPage("/login")
					.defaultSuccessUrl("/user/home")
					.failureUrl("/login?error=true")
					.permitAll())
			.logout(logout -> logout                                                
		            .logoutUrl("/logout")                                            
		            .logoutSuccessUrl("/")                                      
		            .invalidateHttpSession(true)                                        
		            .deleteCookies("JSESSIONID"));
		return http.build();
	}
}
	

