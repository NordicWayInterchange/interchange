package net.nordicway.interchange;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@RestController("/")
public class NeighbourServerStub {

    private static final Logger logger =  LoggerFactory.getLogger(NeighbourServerStub.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${serverstub.queue}")
    private String queue;

    @Value("${serverstub.selector}")
    private String selector = "publicationId = 'N00001:1234' and causeCode = 6";

    @Value("${serverstub.neighbourname}")
    private String neighbourName = "neighbour";

    @Value("${serverstub.hostname}")
    private String hostname = "myhost"; //actually hostname of qpid

    @Value("${serverstub.broker}")
    private String broker = "mybroker";

    private static AtomicReference<Subscription> subscription = new AtomicReference<>();

    @ResponseStatus(HttpStatus.ACCEPTED)
    @RequestMapping(method = RequestMethod.POST, path = "/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
    public SubscriptionResponseApi requestSubscriptions(@RequestBody SubscriptionRequestApi neighbourSubscriptionRequest) {
        logger.debug("Received incoming subscription request: {}", neighbourSubscriptionRequest.toString());

        RequestedSubscriptionApi requestedSubscriptionApi = neighbourSubscriptionRequest.getSubscriptions().stream().findFirst().orElseThrow();

        Subscription inputSubscription = new Subscription("abc", requestedSubscriptionApi.getSelector());
        subscription.compareAndSet(null, inputSubscription);
        return new SubscriptionResponseApi(
                hostname,
                Set.of(
                        new RequestedSubscriptionResponseApi(
                                inputSubscription.id(),
                                inputSubscription.selector(),
                                "/" + neighbourName + "/subscriptions/" +  inputSubscription.id(),
                                SubscriptionStatusApi.REQUESTED,
                                neighbourName
                        )
                )
        );
    }


    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.GET, path = "/{ixnName}/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
    public SubscriptionResponseApi listSubscriptions(@PathVariable(name = "ixnName") String ixnName) {
        logger.info("Received request for subscriptions for neighbour {}", ixnName);
        logger.debug("Common name matches Neighbour name in path.");

        Subscription savedSubscription = subscription.get();
        Set<RequestedSubscriptionResponseApi> requestedSubscriptionResponseApiSet = new HashSet<>();
        if (savedSubscription != null) {
            requestedSubscriptionResponseApiSet.add(new RequestedSubscriptionResponseApi(
                    savedSubscription.id(),
                    savedSubscription.selector(),
                    "/" + neighbourName + "/subscriptions/" + savedSubscription.id(),
                    SubscriptionStatusApi.CREATED
            ));
        }
        return new SubscriptionResponseApi(
                hostname,
                requestedSubscriptionResponseApiSet
        );
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.GET, value = "/{ixnName}/subscriptions/{subscriptionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String pollSubscription(@PathVariable(name = "ixnName") String ixnName, @PathVariable(name = "subscriptionId") String subscriptionId) {
        logger.info("Received poll of subscription {} from neighbour {}.",subscriptionId, ixnName);

        Subscription savedSubscription = subscription.get();
        PollResponse response;
        if  (savedSubscription != null) {
            response = new PollResponse(
                    savedSubscription.id(),
                    "2.0",
                    savedSubscription.selector(),
                    neighbourName,
                    "/" + neighbourName + "/subscriptions/abc",
                    SubscriptionStatusApi.CREATED,
                    Set.of(
                            new DynamicFilterEndpoint(
                                    queue,
                                    broker,
                                    5671,
                                    true
                            )
                    )
            );
        } else {
            throw new RuntimeException("Woops");
        }
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (JsonProcessingException e) {
            logger.error("Caught excption",e);
            throw new RuntimeException(e);
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.DELETE, value = "/{ixnName}/subscriptions/{subscriptionId}")
    @Operation(summary="Delete subscription")
    public void deleteSubscription(@PathVariable(name = "ixnName") String ixnName, @PathVariable(name = "subscriptionId") String subscriptionId) {
        logger.info("Received request to delete subscription {} from neighbour {}.",subscriptionId, ixnName);

        subscription.getAndSet(null);


    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.POST, value = "/capabilities", produces = MediaType.APPLICATION_JSON_VALUE)
    public CapabilitiesApi updateCapabilities(@RequestBody CapabilitiesApi neighbourCapabilities) {

        logger.info("Received capability post: {}", neighbourCapabilities.toString());

        logger.debug("Common name of certificate matches Neighbour name in capability api object.");

        CapabilitiesApi capabilitiesApiResponse = new  CapabilitiesApi(
            "servername",
                Set.of(
                        new CapabilityApi(
                                new DenmApplicationApi(
                                        "NO0001",
                                        "N00001:1234",
                                        "NO",
                                        "1.0",
                                        List.of("123"),
                                        List.of(6, 7) //This will be the header we use to separate using the selector
                                ),
                                new MetadataApi(RedirectStatusApi.OPTIONAL)
                        )
                )
        );
        logger.info("Responding with local capabilities: {}", capabilitiesApiResponse.toString());
        return capabilitiesApiResponse;
    }

    private record DynamicFilterEndpoint(String source, String host, Integer port, @JsonInclude(JsonInclude.Include.NON_NULL) Boolean requiresDynamicFilter) { }

    private record PollResponse(String id, String version, String selector, String consumerCommonName, String path, SubscriptionStatusApi status, Set<DynamicFilterEndpoint> endpoints) {}

    private record Subscription(String id, String selector) {};
}
