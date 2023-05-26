package no.vegvesen.ixn.federation;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.qpid.VirtualHostAccessControlProvider;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class QpidAclTest {


    @Test
    public void loadFromFile() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        VirtualHostAccessControlProvider provider = mapper.readValue(new File("src/test/resources/acl.json"), VirtualHostAccessControlProvider.class);
        assertThat(provider.getId()).isEqualTo("41664015-ab35-4341-bc35-18a63470cf03");

    }
}
