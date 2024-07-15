package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class VirtualHostAccessControllerTest {


    private ObjectMapper mapper;
    private VirtualHostAccessController provider;

    @BeforeEach
    public void loadFile() throws IOException {
        mapper = new ObjectMapper();
        provider = mapper.readValue(new File("src/test/resources/acl.json"), VirtualHostAccessController.class);
    }

    @Test
    public void loadFromFile() throws IOException {
        assertThat(provider.getId()).isEqualTo("41664015-ab35-4341-bc35-18a63470cf03");
    }


    @Test
    public void addQueueReadAccess() throws IOException {
        provider.addQueueReadAccess("test-user","test-queue");
        AclRule rule = VirtualHostAccessController.createQueueReadAccessRule("test-user", "test-queue");
        assertThat(provider.containsRule(rule)).isTrue();
    }

    @Test
    public void addQueueWriteAccess() throws IOException {
        provider.addExchangeWriteAccess("test-user", "test-queue");
        AclRule rule = VirtualHostAccessController.createExchangeWriteAccessRule("test-user","test-queue");
        assertThat(provider.containsRule(rule)).isTrue();
    }

    @Test
    public void removeReadAccess() throws IOException {
        AclRule rule = VirtualHostAccessController.createQueueReadAccessRule("anna","7c971dc4-3bcd-4f99-8275-b35e9cfe8592");
        assertThat(provider.containsRule(rule)).isTrue();
        provider.removeQueueReadAccess("anna", "7c971dc4-3bcd-4f99-8275-b35e9cfe8592");
        assertThat(provider.containsRule(rule)).isFalse();
    }

    @Test
    public void removeWriteAccess() throws IOException {
        String spName = "pilotinterchange.eu.bouvet.pilotinterchange.eu.npra.io.fourc1";
        String queue = "del-d9032905-c5a2-4119-863c-1899b12fbced";
        AclRule rule = VirtualHostAccessController.createExchangeWriteAccessRule(
                spName,
                queue
        );
        assertThat(provider.containsRule(rule)).isTrue();
        provider.removeQueueWriteAccess(spName,queue);
        assertThat(provider.containsRule(rule)).isFalse();

    }

}
