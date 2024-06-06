package no.vegvesen.ixn.federation.qpid;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Disabled("Change and reenable when the qpid client refactoring is done")
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
                template,
                "message_collector");
    }

    @Test
    public void createQueue() {
        String queue = "MyQueueu";
        when(template.postForEntity(anyString(),any(CreateQueueRequest.class),any(Class.class)))
                .thenReturn(new ResponseEntity<>(new Queue(queue),HttpStatus.OK));
        client.createQueue(queue);
        assertThat(infoEvents(appender.list.stream()))
                .hasSize(1);
        assertThat(infoEvents(appender.list.stream())
                .anyMatch(formattedMessageContains(queue)))
                .isTrue();
        assertThat(errorEvents(appender.list.stream()))
                .isEmpty();
    }

    @Test
    public void removeQueue() {
        Queue queue = new Queue("queueName");

        client.removeQueue(queue);
        assertThat(infoEvents(appender.list.stream()))
                .hasSize(1)
                .anyMatch(formattedMessageContains("queueName"));
        assertThat(errorEvents(appender.list.stream()))
                .isEmpty();
    }

    @Test
    public void createDirectExchange() {

        String exchangeName = "exchangeName";
        when(template.postForEntity(anyString(), any(CreateExchangeRequest.class),any(Class.class)))
                .thenReturn(new ResponseEntity<>(new Exchange(exchangeName),HttpStatus.OK));
        client.createDirectExchange(exchangeName);
        assertThat(infoEvents(appender.list.stream()))
                .hasSize(1)
                .anyMatch(formattedMessageContains(exchangeName));
        assertThat(errorEvents(appender.list.stream()))
                .isEmpty();
    }

    @Test
    public void createTopicExchange() {

        String exchangeName = "exchangeName";
        when(template.postForEntity(anyString(), any(CreateExchangeRequest.class),any(Class.class)))
                .thenReturn(new ResponseEntity<>(new Exchange(exchangeName),HttpStatus.OK));
        client.createHeadersExchange(exchangeName);
        assertThat(infoEvents(appender.list.stream()))
                .hasSize(1)
                .anyMatch(formattedMessageContains(exchangeName));
        assertThat(errorEvents(appender.list.stream()))
                .isEmpty();
    }


    @Test
    public void removeExchange() {
        String exchangeName = "someExhange";

        client.removeExchange(new Exchange(exchangeName));
        assertThat(infoEvents(appender.list.stream()))
                .hasSize(1)
                .anyMatch(formattedMessageContains(exchangeName));
        assertThat(errorEvents(appender.list.stream()))
                .isEmpty();

    }

    @Test
    public void addMemberToGroup() {
        String memberName = "groupMember";
        String group = "myGroup";
        when(template.postForEntity(anyString(),any(GroupMember.class),any(Class.class)))
                .thenReturn(new ResponseEntity<>(new GroupMember(memberName),HttpStatus.OK));
        client.addMemberToGroup(memberName, group);
        assertThat(infoEvents(appender.list.stream()))
                .hasSize(1)
                .anyMatch(formattedMessageContains(memberName))
                .anyMatch(formattedMessageContains(group));
        assertThat(errorEvents(appender.list.stream()))
                .isEmpty();
    }

    @Test
    public void removeMemberFromGroup() {
        String memberName = "aMember";
        String groupName = "aGroup";
        client.removeMemberFromGroup(new GroupMember(memberName), groupName);
        assertThat(infoEvents(appender.list.stream()))
                .hasSize(1)
                .anyMatch(formattedMessageContains(memberName))
                .anyMatch(formattedMessageContains(groupName));
        assertThat(errorEvents(appender.list.stream()))
                .isEmpty();

    }

    @Test
    public void addBinding() {

        String selector = "a = b";
        String source = "source";
        String destination = "destination";
        String bindingKey = "bindingKey";
        when(template.postForEntity(anyString(), any(AddBindingRequest.class),any(Class.class)))
                .thenReturn(new ResponseEntity<>(Boolean.TRUE,HttpStatus.OK));
        client.addBinding(source, new Binding(bindingKey, destination, new Filter(selector)));
        assertThat(infoEvents(appender.list.stream()))
                .hasSize(1)
                .anyMatch(formattedMessageContains(selector))
                .anyMatch(formattedMessageContains(source))
                .anyMatch(formattedMessageContains(destination))
                .anyMatch(formattedMessageContains(bindingKey));
        assertThat(errorEvents(appender.list.stream())).isEmpty();
    }

    @Test
    public void postQpidAcl() {
        client.postQpidAcl(new VirtualHostAccessController("myAcl", new ArrayList<>()));
        assertThat(infoEvents(appender.list.stream()))
                .hasSize(1);
        assertThat(errorEvents(appender.list.stream()))
                .isEmpty();
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
