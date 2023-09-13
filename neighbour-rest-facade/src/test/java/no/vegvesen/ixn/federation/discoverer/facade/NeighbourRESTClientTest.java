package no.vegvesen.ixn.federation.discoverer.facade;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitiesSplitApi;
import no.vegvesen.ixn.federation.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NeighbourRESTClientTest {


    private RestTemplate template;
    private ObjectMapper mapper;
    private Logger logger;
    private ListAppender<ILoggingEvent> appender;
    private NeighbourRESTClient client;


    @BeforeEach
    public void setUp() {
        template = mock(RestTemplate.class);
        mapper = new ObjectMapper();
        logger = (Logger) LoggerFactory.getLogger(NeighbourRESTClient.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        client = new NeighbourRESTClient(template, mapper);

    }


    @Test
    public void doPostCapabilitiesOk() {
        when(template.exchange(any(),any(),any(), any(Class.class), (Object[])any()))
                .thenReturn(new ResponseEntity<>(new CapabilitiesSplitApi(), HttpStatus.OK));
        client.doPostCapabilities("https://test.server/","test",new CapabilitiesSplitApi());
        //We don't want the client to emit INFO messages, as we do this in the RESTFacade
        verify(template).exchange(any(),any(),any(),any(Class.class),(Object[])any());
        assertThat(infoEvents(appender)).isEmpty();
        assertThat(errorEvents(appender)).isEmpty();
    }

    @Test
    @DisplayName("Post capability: Server returns OK, but with empty response body")
    public void doPostCapabilitiesOkEmpty() {
        when(template.exchange(any(),any(),any(), any(Class.class), (Object[])any()))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));
        String neighbourName = "test";
        CapabilityPostException thrown = assertThrows(CapabilityPostException.class, () -> client.doPostCapabilities("https://test.server/", neighbourName, new CapabilitiesSplitApi()));
        assertThat(thrown.getMessage()).contains(neighbourName);
        //We don't want the client to emit INFO messages, as we do this in the RESTFacade
        verify(template).exchange(any(),any(),any(),any(Class.class),(Object[])any());
        assertThat(infoEvents(appender)).isEmpty();
        assertThat(errorEvents(appender)).isEmpty();
    }

    @Test
    public void doPostCapabilitiesServerError() {
        HttpServerErrorException exception = HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR,"",null,new byte[0],null);

        when(template.exchange(any(),any(),any(), any(Class.class), (Object[])any()))
                .thenThrow(exception);
        String neighbourName = "test";
        CapabilityPostException thrown = assertThrows(CapabilityPostException.class,
                () -> client.doPostCapabilities("https://test.server/", neighbourName, new CapabilitiesSplitApi()));
        assertThat(thrown.getMessage()).contains(neighbourName);
        verify(template).exchange(any(),any(),any(), any(Class.class), (Object[])any());
        assertThat(infoEvents(appender)).isEmpty();
        assertThat(errorEvents(appender)).isEmpty();
    }

    @Test
    public void doPostCapabilitiesErrorWithErrorDetails() throws JsonProcessingException {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),HttpStatus.INTERNAL_SERVER_ERROR.toString(),"Huston, we have a problem");
        HttpServerErrorException exception = HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR,HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),null,mapper.writeValueAsBytes(errorDetails),null);

        when(template.exchange(any(),any(),any(), any(Class.class), (Object[])any()))
                .thenThrow(exception);

        String neighbourName = "test";
        CapabilityPostException thrown = assertThrows(CapabilityPostException.class,
                () -> client.doPostCapabilities("https://test.server/", neighbourName, new CapabilitiesSplitApi()));
        assertThat(thrown).hasMessageContaining(neighbourName);
        verify(template).exchange(any(),any(),any(), any(Class.class), (Object[])any());
        assertThat(infoEvents(appender)).isEmpty();
        assertThat(errorEvents(appender)).isEmpty();
    }

    @Test
    public void doPostCapabilitiesWithRestClientException() {
        RestClientException exception = new RestClientException("I am a teapot");
        when(template.exchange(any(),any(),any(), any(Class.class), (Object[])any()))
                .thenThrow(exception);

        String neighbourName = "test";
        CapabilityPostException thrown = assertThrows(CapabilityPostException.class,
                () -> client.doPostCapabilities("https://test.server/", neighbourName, new CapabilitiesSplitApi()));
        assertThat(thrown).hasMessageContaining(neighbourName);
        verify(template).exchange(any(),any(),any(), any(Class.class), (Object[])any());
        assertThat(infoEvents(appender)).isEmpty();
        assertThat(errorEvents(appender)).isEmpty();
    }


    @Test
    public void doPostSubscriptionRequestOk() {
        when(template.exchange(any(),any(),any(), any(Class.class), (Object[])any()))
                .thenReturn(new ResponseEntity(new SubscriptionResponseApi(),HttpStatus.OK));

        client.doPostSubscriptionRequest(new SubscriptionRequestApi(),"https://test.server/","test");
        verify(template).exchange(any(),any(),any(), any(Class.class), (Object[])any());
        assertThat(infoEvents(appender)).isEmpty();
        assertThat(errorEvents(appender)).isEmpty();
    }

    @Test
    @DisplayName("Post subscription request: Server returns OK, but with empty response body")
    public void postSubscriptionRequestOkEmpty() {
        when(template.exchange(any(),any(),any(), any(Class.class), (Object[])any()))
                .thenReturn(new ResponseEntity(null,HttpStatus.OK));

        String neighbourName = "test";
        SubscriptionRequestException thrown = assertThrows(SubscriptionRequestException.class, () -> client.doPostSubscriptionRequest(new SubscriptionRequestApi(), "https://test.server/", neighbourName));
        assertThat(thrown).hasMessageContaining(neighbourName);
        verify(template).exchange(any(),any(),any(), any(Class.class), (Object[])any());
        assertThat(infoEvents(appender)).isEmpty();
        assertThat(errorEvents(appender)).isEmpty();
    }

    @Test
    @DisplayName("Post non empty subscription request, get empty one back")
    public void doPostSubscriptionRequestEmpty() {
        when(template.exchange(any(),any(),any(), any(Class.class), (Object[])any()))
                .thenReturn(new ResponseEntity(new SubscriptionResponseApi(),HttpStatus.OK));

        String neighbourName = "test";
        SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi(neighbourName, Collections.singleton(
                new RequestedSubscriptionApi(
                        "a = b"
                )
        ));
        SubscriptionRequestException thrown = assertThrows(SubscriptionRequestException.class, () ->
                client.doPostSubscriptionRequest(subscriptionRequestApi, "https://test.server/", neighbourName));
        assertThat(thrown).hasMessageContaining(neighbourName);
        verify(template).exchange(any(),any(),any(), any(Class.class), (Object[])any());
        assertThat(infoEvents(appender)).isEmpty();
        assertThat(errorEvents(appender)).isEmpty();
    }

    @Test
    public void doPostSubscriptionRequestNoErrorResponse() {
        HttpServerErrorException exception = HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR,"",null,new byte[0],null);

        when(template.exchange(any(),any(),any(), any(Class.class), (Object[])any()))
                .thenThrow(exception);

        String neighbourName = "test";
        SubscriptionRequestException thrown = assertThrows(SubscriptionRequestException.class, () ->
                client.doPostSubscriptionRequest(new SubscriptionRequestApi(), "https://test.server/", neighbourName));
        assertThat(thrown).hasMessageContaining(neighbourName);
        verify(template).exchange(any(),any(),any(), any(Class.class), (Object[])any());
        assertThat(infoEvents(appender)).isEmpty();
        assertThat(errorEvents(appender)).isEmpty();
    }

    @Test
    public void postSubscriptionRequestErrorDetails() throws JsonProcessingException {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),HttpStatus.BAD_REQUEST.toString(),"You're doing it wrong");
        HttpClientErrorException exception = HttpClientErrorException.create(HttpStatus.BAD_REQUEST,HttpStatus.BAD_REQUEST.getReasonPhrase(),null,mapper.writeValueAsBytes(errorDetails),null);

        when(template.exchange(any(),any(),any(), any(Class.class), (Object[])any()))
                .thenThrow(exception);

        String neighbourName = "test";
        SubscriptionRequestException thrown = assertThrows(SubscriptionRequestException.class, () ->
                client.doPostSubscriptionRequest(new SubscriptionRequestApi(), "https://test.server/", neighbourName));
        assertThat(thrown).hasMessageContaining(neighbourName);

        verify(template).exchange(any(),any(),any(), any(Class.class), (Object[])any());
        assertThat(infoEvents(appender)).isEmpty();
        assertThat(errorEvents(appender)).isEmpty();
    }

    @Test
    public void doPostSubscriptionRequestRestClientException() {
        RestClientException exception = new RestClientException("Something is not right");
        when(template.exchange(any(),any(),any(), any(Class.class), (Object[])any()))
                .thenThrow(exception);

        String neighbourName = "test";
        SubscriptionRequestException thrown = assertThrows(SubscriptionRequestException.class, () ->
                client.doPostSubscriptionRequest(new SubscriptionRequestApi(), "https://test.server/", neighbourName));
        assertThat(thrown).hasMessageContaining(neighbourName);

        verify(template).exchange(any(),any(),any(), any(Class.class), (Object[])any());
        assertThat(infoEvents(appender)).isEmpty();
        assertThat(errorEvents(appender)).isEmpty();
    }

    @Test
    public void doPollSubscriptionStatusStatusOk() {
        when(template.getForEntity(any(),any(), (Object[]) any()))
                .thenReturn(new ResponseEntity<>(new SubscriptionPollResponseApi(),HttpStatus.OK));
        client.doPollSubscriptionStatus("https://test.server/","test");
        verify(template).getForEntity(any(),any(),(Object[])any());
        assertThat(infoEvents(appender)).isEmpty();
        assertThat(errorEvents(appender)).isEmpty();
    }

    @Test
    public void doPollSubscriptionStatusServerError() {
        HttpServerErrorException exception = HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR,"",null,new byte[0],null);
        when(template.getForEntity(any(),any(), (Object[]) any()))
                .thenThrow(exception);
        String neighbourName = "test";
        SubscriptionPollException thrown = assertThrows(SubscriptionPollException.class, () -> client.doPollSubscriptionStatus("https://test.server/", neighbourName));
        assertThat(thrown).hasMessageContaining(neighbourName);
        verify(template).getForEntity(any(),any(),(Object[])any());
        assertThat(infoEvents(appender)).isEmpty();
        assertThat(errorEvents(appender)).isEmpty();
    }


    @Test
    public void doPollSubscriptionStatusClientError() throws JsonProcessingException {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),HttpStatus.BAD_REQUEST.toString(),"You're doing it wrong");
        HttpClientErrorException exception = HttpClientErrorException.create(HttpStatus.BAD_REQUEST,HttpStatus.BAD_REQUEST.getReasonPhrase(),null,mapper.writeValueAsBytes(errorDetails),null);
        when(template.getForEntity(any(),any(), (Object[]) any()))
                .thenThrow(exception);
        String neighbourName = "test";
        SubscriptionPollException thrown = assertThrows(SubscriptionPollException.class, () -> client.doPollSubscriptionStatus("https://test.server/", neighbourName));
        assertThat(thrown).hasMessageContaining(neighbourName);
        verify(template).getForEntity(any(),any(),(Object[])any());
        assertThat(infoEvents(appender)).isEmpty();
        assertThat(errorEvents(appender)).isEmpty();
    }

    @Test
    public void doPollSubscriptionStatusNotFound() throws JsonProcessingException {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),HttpStatus.NOT_FOUND.toString(),"You're doing it wrong");
        HttpClientErrorException exception = HttpClientErrorException.create(HttpStatus.NOT_FOUND,HttpStatus.NOT_FOUND.getReasonPhrase(),null,mapper.writeValueAsBytes(errorDetails),null);
        when(template.getForEntity(any(),any(), (Object[]) any()))
                .thenThrow(exception);
        String neighbourName = "test";
        SubscriptionNotFoundException thrown = assertThrows(SubscriptionNotFoundException.class, () -> client.doPollSubscriptionStatus("https://test.server/", neighbourName));
        assertThat(thrown).hasMessageContaining(neighbourName);
        verify(template).getForEntity(any(),any(),(Object[])any());
        assertThat(infoEvents(appender)).isEmpty();
        assertThat(errorEvents(appender)).isEmpty();
    }

    @Test
    public void doPollSubscriptionStatusRestClientException() {
        RestClientException exception = new RestClientException("No-go");
        when(template.getForEntity(any(),any(), (Object[]) any()))
                .thenThrow(exception);
        String neighbourName = "test";
        SubscriptionPollException thrown = assertThrows(SubscriptionPollException.class, () -> client.doPollSubscriptionStatus("https://test.server/", neighbourName));
        assertThat(thrown).hasMessageContaining(neighbourName);
        verify(template).getForEntity(any(),any(),(Object[])any());
        assertThat(infoEvents(appender)).isEmpty();
        assertThat(errorEvents(appender)).isEmpty();
    }

    @Test
    public void deleteSubscriptionOk() {
        doNothing().when(template).delete(any(), (Object[])any());
        client.deleteSubscriptions("https://test.server/","test");
        verify(template).delete(any(), (Object[])any());
        assertThat(infoEvents(appender)).isEmpty();
        assertThat(errorEvents(appender)).isEmpty();
    }

    @Test
    public void deleteSubscriptionServerException() {
        HttpServerErrorException exception = HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR,"",null,new byte[0],null);
        doThrow(exception).when(template).delete(any(), (Object[])any());
        String neighbourName = "test";
        SubscriptionDeleteException thrown = assertThrows(SubscriptionDeleteException.class,
                () -> client.deleteSubscriptions("https://test.server/", neighbourName));
        assertThat(thrown).hasMessageContaining(neighbourName);
        verify(template).delete(any(), (Object[])any());
        assertThat(infoEvents(appender)).isEmpty();
        assertThat(errorEvents(appender)).isEmpty();
    }

    @Test
    public void deleteSubscriptionNotFoundException() {
        HttpClientErrorException exception = HttpClientErrorException.create(HttpStatus.NOT_FOUND,HttpStatus.NOT_FOUND.getReasonPhrase(),null,new byte[0],null);
        doThrow(exception).when(template).delete(any(), (Object[])any());
        String neighbourName = "test";
        SubscriptionNotFoundException thrown = assertThrows(SubscriptionNotFoundException.class,
                () -> client.deleteSubscriptions("https://test.server/", neighbourName));
        assertThat(thrown).hasMessageContaining(neighbourName);
        verify(template).delete(any(), (Object[])any());
        assertThat(infoEvents(appender)).isEmpty();
        assertThat(errorEvents(appender)).isEmpty();
    }

    private static Stream<ILoggingEvent> infoEvents(ListAppender<ILoggingEvent> appender) {
        return logStream(appender, filterInfoEvents());
    }

    private static Stream<ILoggingEvent> errorEvents(ListAppender<ILoggingEvent> appender) {
        return logStream(appender,filterErrorEvents());
    }

    private static Predicate<ILoggingEvent> filterInfoEvents() {
        return e -> e.getLevel().equals(Level.INFO);
    }

    private static Predicate<ILoggingEvent> filterErrorEvents() {
        return e -> e.getLevel().equals(Level.ERROR);
    }

    private static Stream<ILoggingEvent> logStream(ListAppender<ILoggingEvent> appender, Predicate<ILoggingEvent> infoEvent) {
        return getStream(appender).filter(infoEvent);
    }

    private static Stream<ILoggingEvent> getStream(ListAppender<ILoggingEvent> appender) {
        return appender.list.stream();
    }



}
