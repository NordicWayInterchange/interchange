package no.vegvesen.ixn.federation.serviceproviderclient.command.auth;

import no.vegvesen.ixn.federation.serviceproviderclient.token.StoredToken;
import no.vegvesen.ixn.federation.serviceproviderclient.token.TokenFetcher;
import no.vegvesen.ixn.federation.serviceproviderclient.token.TokenResponse;
import no.vegvesen.ixn.federation.serviceproviderclient.token.TokenStore;
import picocli.CommandLine.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;

@Command(
        name = "login",
        description = "Log in to Keycloak using the client_credentials grant, and cache the access token " +
                "so that other serviceproviderclient commands can use it. Run this before using any other command.",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """
                        Example:\n
                        serviceproviderclient login --token-endpoint=https://keycloak.example.com/realms/myrealm/protocol/openid-connect/token --client-id=myclient --client-secret=mysecret
                        """
        }
)
public class LoginCommand implements Callable<Integer> {

    @Option(names = {"--token-endpoint"}, required = true, description = "URL of the Keycloak (or other OIDC compliant) token endpoint")
    private String tokenEndpoint;

    @Option(names = {"--client-id"}, required = true, description = "The client id to authenticate with")
    private String clientId;

    @Option(names = {"--client-secret"}, required = true, description = "The client secret to authenticate with")
    private String clientSecret;

    @Override
    public Integer call() throws Exception {
        TokenFetcher tokenFetcher = new TokenFetcher(tokenEndpoint, clientId, clientSecret);
        TokenResponse tokenResponse = tokenFetcher.fetchTokenResponse();

        long expiresInSeconds = tokenResponse.getExpiresIn() != null ? tokenResponse.getExpiresIn() : 60;
        long expiresAtEpochMillis = System.currentTimeMillis() + (expiresInSeconds * 1000);

        StoredToken storedToken = new StoredToken(
                tokenResponse.getAccessToken(), tokenEndpoint, clientId, expiresAtEpochMillis);
        new TokenStore().save(storedToken);

        System.out.printf(
                "Login successful. Access token is valid until %s.%n",
                DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(expiresAtEpochMillis)));
        return 0;
    }
}

