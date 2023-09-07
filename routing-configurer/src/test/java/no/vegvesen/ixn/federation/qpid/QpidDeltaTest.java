package no.vegvesen.ixn.federation.qpid;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class QpidDeltaTest {


    @Test
    public void testFindQueueByName() {
        Queue queue1 = new Queue("1");
        Queue queue2 = new Queue("2");
        QpidDelta delta = new QpidDelta(Collections.emptyList(), Arrays.asList(queue1,queue2));
        assertThat(delta.findByQueueName("1")).isEqualTo(queue1);

        assertThat(delta.findByQueueName("3")).isNull();

    }
}
