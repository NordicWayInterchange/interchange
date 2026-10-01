package no.vegvesen.ixn.serviceprovider.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class OnboardSecurityConfig {
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(Customizer.withDefaults())
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.NEVER)
                )
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

}
