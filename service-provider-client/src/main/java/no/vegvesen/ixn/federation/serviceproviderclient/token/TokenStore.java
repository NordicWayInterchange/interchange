package no.vegvesen.ixn.federation.serviceproviderclient.token;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Set;

import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

public class TokenStore {

    public static final String TOKEN_FILE_ENV_VAR = "SERVICEPROVIDERCLIENT_TOKEN_FILE";

    private static final Path DEFAULT_TOKEN_FILE = defaultTokenFile();

    private final Path tokenFile;
    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    public TokenStore() {
        this(DEFAULT_TOKEN_FILE);
    }

    TokenStore(Path tokenFile) {
        this.tokenFile = tokenFile;
    }

    private static Path defaultTokenFile() {
        String override = System.getenv(TOKEN_FILE_ENV_VAR);
        if (override != null && !override.isBlank()) {
            return Paths.get(override);
        }
        return Paths.get(System.getProperty("user.home"), ".serviceproviderclient", "token.json");
    }

    public void save(StoredToken token) {
        try {
            Path parent = tokenFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(tokenFile, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(token));
            restrictPermissionsToOwner();
        } catch (IOException e) {
            throw new TokenFetchException("Could not save access token to " + tokenFile, e);
        }
    }

    private void restrictPermissionsToOwner() {
        try {
            Set<java.nio.file.attribute.PosixFilePermission> ownerOnly = EnumSet.of(OWNER_READ, OWNER_WRITE);
            Files.setPosixFilePermissions(tokenFile, ownerOnly);
        } catch (UnsupportedOperationException | IOException ignored) {
        }
    }

    public StoredToken load() {
        if (!Files.exists(tokenFile)) {
            return null;
        }
        try {
            return objectMapper.readValue(Files.readString(tokenFile), StoredToken.class);
        } catch (Exception e) {
            return null;
        }
    }

    public StoredToken loadIfValid() {
        StoredToken token = load();
        return (token != null && token.isValid()) ? token : null;
    }

    public void clear() {
        try {
            Files.deleteIfExists(tokenFile);
        } catch (IOException e) {
            throw new TokenFetchException("Could not remove stored access token at " + tokenFile, e);
        }
    }
}

