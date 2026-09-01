package no.vegvesen.ixn.serviceprovider.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class JwtIdentityResolver implements IdentityResolver {
    @Override
    public String getCallerName() {
        Authentication principal = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) principal;
        return Objects.requireNonNull(jwtAuthenticationToken).getToken().getClaimAsString("organization");
    }
}
