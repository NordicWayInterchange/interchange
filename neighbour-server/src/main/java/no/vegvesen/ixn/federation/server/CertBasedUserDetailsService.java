package no.vegvesen.ixn.federation.server;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CertBasedUserDetailsService implements UserDetailsService{


	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		UserDetails user = User.withUsername(userName).password("").roles("USER").build();
		return user;
	}
}

