package no.vegvesen.ixn.serviceprovider.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public class JwtIdentityResolver implements IdentityResolver {

    Logger logger = LoggerFactory.getLogger(JwtIdentityResolver.class);

    @Override
    public String getCallerName() {
        Authentication principal = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) principal;
        return Objects.requireNonNull(jwtAuthenticationToken).getToken().getClaimAsString("client_id");
    }
}
