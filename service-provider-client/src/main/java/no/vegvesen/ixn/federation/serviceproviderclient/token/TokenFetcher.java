package no.vegvesen.ixn.federation.serviceproviderclient.token;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class TokenFetcher {

    private static final String GRANT_TYPE = "client_credentials";

    private final String tokenEndpoint;
    private final String clientId;
    private final String clientSecret;
    private final RestTemplate restTemplate;

    public TokenFetcher(String tokenEndpoint, String clientId, String clientSecret) {
        this(tokenEndpoint, clientId, clientSecret, new RestTemplate());
    }

    TokenFetcher(String tokenEndpoint, String clientId, String clientSecret, RestTemplate restTemplate) {
        this.tokenEndpoint = tokenEndpoint;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restTemplate = restTemplate;
    }

    public String fetchToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", GRANT_TYPE);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<TokenResponse> response;
        try {
            response = restTemplate.postForEntity(tokenEndpoint, request, TokenResponse.class);
        } catch (RestClientException e) {
            throw new TokenFetchException("Failed to fetch token from token endpoint " + tokenEndpoint, e);
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new TokenFetchException(
                    "Token endpoint " + tokenEndpoint + " returned status " + response.getStatusCode().value());
        }

        TokenResponse tokenResponse = response.getBody();
        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            throw new TokenFetchException("Token endpoint " + tokenEndpoint + " did not return an access token");
        }

        return tokenResponse.getAccessToken();
    }
}
