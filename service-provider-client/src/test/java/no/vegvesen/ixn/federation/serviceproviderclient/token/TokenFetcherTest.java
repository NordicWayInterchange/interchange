package no.vegvesen.ixn.federation.serviceproviderclient.token;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

class TokenFetcherTest {

    private static final String TOKEN_ENDPOINT = "http://test-endpoint";


    private final RestTemplate restTemplate = new RestTemplate();
    private final MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
    private final TokenFetcher tokenFetcher = new TokenFetcher(TOKEN_ENDPOINT, "my-client", "my-secret", restTemplate);

    @Test
    void fetchTokenSendsClientCredentialsRequestAndReturnsAccessToken() {
        mockServer.expect(requestTo(TOKEN_ENDPOINT))
                .andExpect(method(POST))
                .andExpect(content().contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().string(allOf(
                        containsString("grant_type=client_credentials"),
                        containsString("client_id=my-client"),
                        containsString("client_secret=my-secret")
                )))
                .andRespond(withSuccess(
                        "{\"access_token\":\"the-access-token\",\"token_type\":\"Bearer\",\"expires_in\":300}",
                        MediaType.APPLICATION_JSON));

        String token = tokenFetcher.fetchToken();

        assertThat(token).isEqualTo("the-access-token");
        mockServer.verify();
    }

    @Test
    void fetchTokenThrowsWhenTokenEndpointReturnsErrorStatus() {
        mockServer.expect(requestTo(TOKEN_ENDPOINT))
                .andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(tokenFetcher::fetchToken)
                .isInstanceOf(TokenFetchException.class);
    }

    @Test
    void fetchTokenThrowsWhenResponseHasNoAccessToken() {
        mockServer.expect(requestTo(TOKEN_ENDPOINT))
                .andExpect(method(POST))
                .andRespond(withSuccess("{\"token_type\":\"Bearer\"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(tokenFetcher::fetchToken)
                .isInstanceOf(TokenFetchException.class);
    }
}
