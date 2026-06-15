package no.vegvesen.ixn.docker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.OrganizationRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

@Testcontainers
public class KeycloakTest {

    private final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();
    @Container
    private KeycloakContainer  keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:26.6.0")
            .withAdminUsername("admin")
            .withAdminPassword("admin");


    @Test
    @Disabled
    public void testWhereWeCanCreateARealmAndGetTheJson() throws JsonProcessingException {
        System.out.println("Auth server url:" + keycloakContainer.getAuthServerUrl());
        Keycloak keycloakAdminClient = keycloakContainer.getKeycloakAdminClient();
        RealmRepresentation realmRepresentation = keycloakAdminClient.realm("Test").toRepresentation();
        System.out.println(objectWriter.writeValueAsString(realmRepresentation));

    }

    @Test
    public void testImportAVeryLimitedRealm() throws JsonProcessingException, InterruptedException {
        String realmName = "Test";


        //TODO 4 users total, 1 with org1, 1 with org2, one with no org, 1 with both
        UserRepresentation user = new UserRepresentation();
        user.setUsername("test-user@test.com");
        user.setEmail("test-user@test.com");
        user.setFirstName("test");
        user.setLastName("testesen");
        user.setEnabled(true);

        CredentialRepresentation credentials = new CredentialRepresentation();
        credentials.setType("password");
        credentials.setValue("password");
        credentials.setTemporary(false);
        user.setCredentials(List.of(
                credentials
        ));

        //TODO root url, origns og admin url
        ClientRepresentation client = new ClientRepresentation();
        client.setClientId("test-client");
        RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setRealm(realmName);
        realmRepresentation.setClients(List.of(client));
        realmRepresentation.setUsers(List.of(user));
        realmRepresentation.setOrganizationsEnabled(true);

        //TODO Organization
        OrganizationRepresentation organization = new OrganizationRepresentation();
        organization.setName("Bouvet");
        realmRepresentation.setOrganizations(List.of(organization));

        System.out.println(objectWriter.writeValueAsString(realmRepresentation));
        System.out.println("Auth server url:" + keycloakContainer.getAuthServerUrl());
        Keycloak keycloakAdminClient = keycloakContainer.getKeycloakAdminClient();
        keycloakAdminClient.realms().create(realmRepresentation);

        //System.out.println(objectWriter.writeValueAsString(resultingRepresentation));

    }

}
