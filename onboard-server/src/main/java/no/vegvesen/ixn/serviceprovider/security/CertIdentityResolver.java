package no.vegvesen.ixn.serviceprovider.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CertIdentityResolver implements IdentityResolver {
    @Override
    public String getCallerName() {
        Authentication principal = SecurityContextHolder.getContext().getAuthentication();
        return Objects.requireNonNull(principal).getName();
    }
}
