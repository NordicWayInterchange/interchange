package no.vegvesen.ixn.federation.serviceproviderclient.token;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class TokenStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void loadReturnsNullWhenNoTokenHasBeenSaved() {
        TokenStore tokenStore = new TokenStore(tempDir.resolve("token.json"));

        assertThat(tokenStore.load()).isNull();
        assertThat(tokenStore.loadIfValid()).isNull();
    }

    @Test
    void savedTokenCanBeLoadedBack() {
        TokenStore tokenStore = new TokenStore(tempDir.resolve("token.json"));
        long expiresAt = System.currentTimeMillis() + 60_000;
        StoredToken token = new StoredToken("the-access-token", "http://token-endpoint", "my-client", expiresAt);

        tokenStore.save(token);
        StoredToken loaded = tokenStore.load();

        assertThat(loaded).isNotNull();
        assertThat(loaded.getAccessToken()).isEqualTo("the-access-token");
        assertThat(loaded.getTokenEndpoint()).isEqualTo("http://token-endpoint");
        assertThat(loaded.getClientId()).isEqualTo("my-client");
        assertThat(loaded.getExpiresAtEpochMillis()).isEqualTo(expiresAt);
        assertThat(loaded.isValid()).isTrue();
        assertThat(tokenStore.loadIfValid()).isNotNull();
    }

    @Test
    void loadIfValidReturnsNullWhenTokenHasExpired() {
        TokenStore tokenStore = new TokenStore(tempDir.resolve("token.json"));
        StoredToken expiredToken = new StoredToken(
                "the-access-token", "http://token-endpoint", "my-client", System.currentTimeMillis() - 1_000);

        tokenStore.save(expiredToken);

        assertThat(tokenStore.load()).isNotNull();
        assertThat(tokenStore.loadIfValid()).isNull();
    }

    @Test
    void clearRemovesTheStoredToken() {
        TokenStore tokenStore = new TokenStore(tempDir.resolve("token.json"));
        tokenStore.save(new StoredToken("token", "endpoint", "client", System.currentTimeMillis() + 60_000));

        tokenStore.clear();

        assertThat(tokenStore.load()).isNull();
    }
}

