package no.vegvesen.ixn.federation.qpid;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.type.CollectionType;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class QueueTest {

    @Test
    public void readQueuesFromJsonFile() throws IOException {
        File file = new File("src/test/resources/queues.json");
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory typeFactory = mapper.getTypeFactory();

        CollectionType collectionType = typeFactory.constructCollectionType(
                List.class, Queue.class);

        List<Queue> result = mapper.readValue(file, collectionType);
        assertThat(result.size()).isEqualTo(4);
    }

}
