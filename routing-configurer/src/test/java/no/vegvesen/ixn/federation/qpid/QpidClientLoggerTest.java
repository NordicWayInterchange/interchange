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

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QpidClientLoggerTest {

    @Test
    public void testFoo() {
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
        client.removeQueue("test");
        assertTrue(appender.list.stream().anyMatch(e -> e.getLevel().equals(Level.INFO)));
    }

}
