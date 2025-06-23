package no.vegvesen.ixn.federation.qpid;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


public class GroupTest {

    @Test
    public void readGroupFromJson() throws IOException {
        String object = """
{
  "id" : "0bcd5aff-b165-4b81-92c3-c43f31218a9b",
  "name" : "service-providers",
  "type" : "ManagedGroup",
  "desiredState" : "ACTIVE",
  "state" : "ACTIVE",
  "durable" : true,
  "lifetimePolicy" : "PERMANENT",
  "lastOpenedTime" : 1739544826715,
  "createdTime" : 1739544826714
}
""";
        ObjectMapper mapper = new ObjectMapper();
        Group result = mapper.readValue(object,Group.class);
        assertThat(result.getId()).isEqualTo("0bcd5aff-b165-4b81-92c3-c43f31218a9b");
    }
}
