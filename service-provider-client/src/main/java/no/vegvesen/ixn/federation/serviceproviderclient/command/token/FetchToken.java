package no.vegvesen.ixn.federation.serviceproviderclient.command.token;

import no.vegvesen.ixn.federation.serviceproviderclient.token.TokenFetcher;
import picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Command(
        name = "token",
        description = "Fetch an OAuth2 access token from a Keycloak token endpoint using the client_credentials grant",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """
                        Example:\n
                        serviceproviderclient token --token-endpoint=https://keycloak.example.com/realms/myrealm/protocol/openid-connect/token --client-id=myclient --client-secret=mysecret
                        """
        }
)
public class FetchToken implements Callable<Integer> {

    @Option(names = {"--token-endpoint"}, required = true, description = "URL of the Keycloak (or other OIDC compliant) token endpoint")
    private String tokenEndpoint;

    @Option(names = {"--client-id"}, required = true, description = "The client id to authenticate with")
    private String clientId;

    @Option(names = {"--client-secret"}, required = true, description = "The client secret to authenticate with")
    private String clientSecret;

    @Override
    public Integer call() throws Exception {
        TokenFetcher tokenFetcher = new TokenFetcher(tokenEndpoint, clientId, clientSecret);
        String token = tokenFetcher.fetchToken();
        System.out.println(token);
        return 0;
    }
}
