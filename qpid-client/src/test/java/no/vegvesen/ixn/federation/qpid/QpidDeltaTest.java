package no.vegvesen.ixn.federation.qpid;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QpidDeltaTest {


    @Test
    public void testFindQueueByName() {
        Queue queue1 = new Queue("1");
        Queue queue2 = new Queue("2");
        QpidDelta delta = new QpidDelta(Arrays.asList(), Arrays.asList(queue1,queue2));
        assertThat(delta.findByQueueName("1")).isEqualTo(queue1);

        assertThat(delta.findByQueueName("3")).isNull();

    }

    @Test
    public void testFindExchangeByName() {
        Exchange exchange1 = new Exchange("1");
        Exchange exchange2 = new Exchange("2");
        QpidDelta delta = new QpidDelta(Arrays.asList(exchange1,exchange2),Arrays.asList());
        assertThat(delta.findByExchangeName("1")).isEqualTo(exchange1);

        assertThat(delta.findByExchangeName("3")).isNull();

    }

    @Test
    public void testFindExchangeByNameNullParameter() {
        Exchange exchange1 = new Exchange("1");
        Exchange exchange2 = new Exchange("2");
        QpidDelta delta = new QpidDelta(Arrays.asList(exchange1,exchange2),Arrays.asList());
        assertThat(delta.findByExchangeName(null)).isNull();

    }

    @Test
    public void testAddingBindingToExchange() {
        QpidDelta delta = new QpidDelta(
                List.of(new Exchange("E1")),
                List.of(new Queue("Q1"))
        );
        Exchange e1 = delta.findByExchangeName("E1");
        assertThat(e1.isBoundTo("Q1")).isFalse();

        Binding binding = new Binding("b1","Q1",new Filter("test"));
        e1.addBinding(binding);
        assertThat(e1.isBoundTo("Q1")).isTrue();
    }

}
