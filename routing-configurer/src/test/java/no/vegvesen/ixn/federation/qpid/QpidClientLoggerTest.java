package no.vegvesen.ixn.federation.qpid;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class QpidClientLoggerTest {


    @Test
    public void createQueue() {
            RestTemplate template = mock(RestTemplate.class);
            Logger logger = (Logger) LoggerFactory.getLogger(QpidClient.class);
            ListAppender<ILoggingEvent> appender = new ListAppender<>();
            appender.start();
            logger.addAppender(appender);
            QpidClient client = new QpidClient(
                    "thisisaUrl",
                    "thisisiaVhost",
                    template
            );
            String queue = "MyQueueu";
        when(template.getForEntity(any(), any())).thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        client.createQueue(queue);
        assertThat(infoEvents(appender.list.stream())).hasSize(1);
        assertThat(infoEvents(appender.list.stream()).anyMatch(formattedMessageContains(queue))).isTrue();
    }

    @Test
    public void removeQueue() {
        RestTemplate template = mock(RestTemplate.class);
        Logger logger = (Logger) LoggerFactory.getLogger(QpidClient.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        QpidClient client = new QpidClient(
                "thisisaUrl",
                "thisisiaVhost",
                template
        );
        when(template.getForEntity(any(), any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        String queueName = "queueName";
        client.removeQueue(queueName);
        assertThat(infoEvents(appender.list.stream())).hasSize(1);
        assertThat(infoEvents(appender.list.stream()).anyMatch(formattedMessageContains(queueName))).isTrue();
        verify(template).getForEntity(any(),any());
    }

    private static Stream<ILoggingEvent> infoEvents(Stream<ILoggingEvent> stream) {
        return stream.filter(eventIsInfo());

    }

    private static Predicate<ILoggingEvent> formattedMessageContains(String queueName) {
        return e -> e.getFormattedMessage().contains(queueName);
    }

    private static Predicate<ILoggingEvent> eventIsInfo() {
        return e -> e.getLevel().equals(Level.INFO);
    }

}
