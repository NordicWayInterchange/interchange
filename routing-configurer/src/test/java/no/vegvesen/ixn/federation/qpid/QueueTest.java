package no.vegvesen.ixn.federation.qpid;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
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
        Queue resultQueue = result.get(0);
        assertThat(resultQueue.getId()).isEqualTo("38e2a596-0e14-429a-9e77-26fd55c41d19");
        assertThat(resultQueue.getMaximumMessageTtl()).isEqualTo(900000l);
        assertThat(resultQueue.getDurable()).isTrue();
    }

    @Test
    public void testJsonWithOnlyName() throws JsonProcessingException {
        Queue queue = new Queue("myQueue");
        String result = new ObjectMapper().writeValueAsString(queue);
        assertThat(result).doesNotContain("id");
        assertThat(result).doesNotContain("durable");
        assertThat(result).doesNotContain("maximumMessageTtl");
    }

    @Test
    public void testJsonWithNameAndTtl() throws JsonProcessingException {
        Queue queue = new Queue("myQueue",1000l);
        String result = new ObjectMapper().writeValueAsString(queue);
        assertThat(result).doesNotContain("id");
        assertThat(result).doesNotContain("durable");
        assertThat(result).contains("maximumMessageTtl");
    }

    @Test
    public void testJsonWithNameAndId() throws JsonProcessingException {
        Queue queue = new Queue("myQueue","1234");
        String result = new ObjectMapper().writeValueAsString(queue);
        assertThat(result).contains("id");
        assertThat(result).doesNotContain("durable");
        assertThat(result).doesNotContain("maximumMessageTtl");
    }

    @Test
    public void testJsonWithNameAndDurable() throws JsonProcessingException {
        Queue queue = new Queue("myQueue",true);
        String result = new ObjectMapper().writeValueAsString(queue);
        assertThat(result).contains("durable");
        assertThat(result).doesNotContain("id");
        assertThat(result).doesNotContain("maximumMessageTtl");
    }

}
