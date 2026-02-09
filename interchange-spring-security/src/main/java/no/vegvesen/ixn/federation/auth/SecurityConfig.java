package no.vegvesen.ixn.federation.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(auth -> auth
						.anyRequest().authenticated()
				)
				.x509(x509 -> x509
						.subjectPrincipalRegex("CN=(.*?)(?:,|$)")
						.userDetailsService(createUserDetailsService())
				)
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.NEVER)
				)
				.csrf(csrf -> csrf.disable());
		return http.build();
	}

	public UserDetailsService createUserDetailsService() {
		return new CertBasedUserDetailsService();
	}

}
