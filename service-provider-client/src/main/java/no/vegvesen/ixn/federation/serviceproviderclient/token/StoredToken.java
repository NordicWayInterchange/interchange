package no.vegvesen.ixn.federation.serviceproviderclient.token;

public class StoredToken {

    private String accessToken;
    private String tokenEndpoint;
    private String clientId;
    private long expiresAtEpochMillis;

    public StoredToken() {
    }

    public StoredToken(String accessToken, String tokenEndpoint, String clientId, long expiresAtEpochMillis) {
        this.accessToken = accessToken;
        this.tokenEndpoint = tokenEndpoint;
        this.clientId = clientId;
        this.expiresAtEpochMillis = expiresAtEpochMillis;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public long getExpiresAtEpochMillis() {
        return expiresAtEpochMillis;
    }

    public void setExpiresAtEpochMillis(long expiresAtEpochMillis) {
        this.expiresAtEpochMillis = expiresAtEpochMillis;
    }

    public boolean isValid() {
        return accessToken != null && !accessToken.isBlank() && System.currentTimeMillis() < expiresAtEpochMillis;
    }
}

