package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.serviceprovider.security.JwtIdentityResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JwtIdentityResolverTest {
    private final JwtIdentityResolver resolver = new JwtIdentityResolver();
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        jwt = Jwt.withTokenValue("token-value")
                .header("abc", "value")
                .claim("organization", "test-org")
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void happyPath() {
        Authentication authentication = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String callerName = resolver.getCallerName();

        assertThat(callerName).isEqualTo("test-org");
    }

    @Test
    void jwtWithoutOrganizationClaimReturnsNull() {
        Jwt jwtWithoutOrganization = Jwt.withTokenValue("token-value")
                .header("abc", "value")
                .claim("other-claim", "some-value")
                .build();
        Authentication authentication = new JwtAuthenticationToken(jwtWithoutOrganization);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String callerName = resolver.getCallerName();

        assertThat(callerName).isNull();
    }

    @Test
    void noAuthenticationInContextThrowsException() {
        assertThrows(NullPointerException.class, resolver::getCallerName);
    }
}
