package no.vegvesen.ixn.federation.qpid;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class QpidClientLoggerTest {


    private RestTemplate template;
    private Logger logger;
    private ListAppender<ILoggingEvent> appender;
    private QpidClient client;


    @BeforeEach
    public void setUp() {
        template = mock(RestTemplate.class);
        logger = (Logger) LoggerFactory.getLogger(QpidClient.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        client = new QpidClient(
                "thisisaUrl",
                "thisisiaVhost",
                template
        );
    }

    @Test
    public void createQueue() {
        String queue = "MyQueueu";
        HttpClientErrorException exception = HttpClientErrorException.create(HttpStatus.NOT_FOUND,"",null,new byte[0],null);
        when(template.getForEntity(any(), any())).thenThrow(exception);
        client.createQueue(queue);
        assertThat(infoEvents(appender.list.stream())).hasSize(1);
        assertThat(infoEvents(appender.list.stream()).anyMatch(formattedMessageContains(queue))).isTrue();
        assertThat(errorEvents(appender.list.stream())).isEmpty();
    }

    @Test
    public void removeQueue() {
        when(template.getForEntity(any(), any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        String queueName = "queueName";
        client.removeQueue(queueName);
        assertThat(infoEvents(appender.list.stream())).hasSize(1);
        assertThat(infoEvents(appender.list.stream()).anyMatch(formattedMessageContains(queueName))).isTrue();
        assertThat(errorEvents(appender.list.stream())).isEmpty();
        verify(template).getForEntity(any(),any());
    }

    @Test
    public void createDirectExchange() {

        when(template.getForEntity(any(), any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        String exchangeName = "exchangeName";
        client.createDirectExchange(exchangeName);
        assertThat(infoEvents(appender.list.stream())).hasSize(1).anyMatch(formattedMessageContains(exchangeName));
        assertThat(errorEvents(appender.list.stream())).isEmpty();
        verify(template).getForEntity(any(),any());
    }

    @Test
    public void createTopicExchange() {

        when(template.getForEntity(any(), any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        String exchangeName = "exchangeName";
        client.createTopicExchange(exchangeName);
        assertThat(infoEvents(appender.list.stream())).hasSize(1).anyMatch(formattedMessageContains(exchangeName));
        assertThat(errorEvents(appender.list.stream())).isEmpty();
        verify(template).getForEntity(any(),any());
    }

    @Test
    public void addBinding() {

        String selector = "a = b";
        String source = "source";
        String destination = "destination";
        String bindingKey = "bindingKey";
        client.addBinding(selector, source, destination, bindingKey);
        assertThat(infoEvents(appender.list.stream()))
                .hasSize(1)
                .anyMatch(formattedMessageContains(selector))
                .anyMatch(formattedMessageContains(source))
                .anyMatch(formattedMessageContains(destination))
                .anyMatch(formattedMessageContains(bindingKey));
        assertThat(errorEvents(appender.list.stream())).isEmpty();
    }

    private static Stream<ILoggingEvent> infoEvents(Stream<ILoggingEvent> stream) {
        return stream.filter(eventIsInfo());
    }

    private static Stream<ILoggingEvent> errorEvents(Stream<ILoggingEvent> stream) {
        return stream.filter(eventIsError());
    }

    private static Predicate<ILoggingEvent> formattedMessageContains(String queueName) {
        return e -> e.getFormattedMessage().contains(queueName);
    }

    private static Predicate<ILoggingEvent> eventIsInfo() {
        return e -> e.getLevel().equals(Level.INFO);
    }

    private static Predicate<ILoggingEvent> eventIsError() {
        return e -> e.getLevel().equals(Level.ERROR);
    }

}
