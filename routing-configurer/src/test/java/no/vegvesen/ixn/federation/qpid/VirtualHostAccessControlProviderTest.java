package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.docker.DockerBaseIT;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class VirtualHostAccessControlProviderTest {


    @Test
    public void loadFromFile() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        VirtualHostAccessControlProvider provider = mapper.readValue(new File("src/test/resources/acl.json"), VirtualHostAccessControlProvider.class);
        assertThat(provider.getId()).isEqualTo("41664015-ab35-4341-bc35-18a63470cf03");
    }


    //TODO more testing
    @Test
    public void addQueueReadAccess() throws IOException {
        //NewAclRule rule = VirtualHostAccessControlProvider.createQueueReadAccessRule("my_reading_client","my_queue");
        ObjectMapper mapper = new ObjectMapper();
        VirtualHostAccessControlProvider provider = mapper.readValue(new File("src/test/resources/acl.json"), VirtualHostAccessControlProvider.class);
        provider.addQueueReadAccess("test-user","test-queue");
        Path targetFolderPathForTestClass = DockerBaseIT.getTargetFolderPathForTestClass(VirtualHostAccessControlProviderTest.class);
        Files.createDirectories(targetFolderPathForTestClass);
        mapper.writer().withDefaultPrettyPrinter().writeValue(targetFolderPathForTestClass.resolve("newAcl.json").toFile(),provider);
    }

}
