package no.vegvesen.ixn.federation.adminserver.qpid;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.vegvesen.ixn.federation.adminserver.properties.AdminQpidClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@ConfigurationPropertiesScan
public class AdminQpidClient {

    public final static long MAX_TTL_15_MINUTES = 900_000L;

    private final Logger logger = LoggerFactory.getLogger(AdminQpidClient.class);

    private static final String EXCHANGE_URL_PATTERN = "%s/api/latest/exchange/default/%s";

    private static final String ALL_QUEUES_URL_PATTERN = "%s/api/latest/queue/default/";

    private static final String ALL_EXCHANGES_URL_PATTERN = "%s/api/latest/exchange/default/";

    public static final String QUEUES_URL_PATTERN = "%s/api/latest/queue/default/%s";

    private final String exchangesURL;
    private final String queuesURL;
    private final RestTemplate restTemplate;
    private final String allQueuesUrl;
    private final String allExchangesUrl;

    public AdminQpidClient(String baseUrl,
                           String vhostName,
                           RestTemplate restTemplate) {
        this.exchangesURL = String.format(EXCHANGE_URL_PATTERN, baseUrl, vhostName);
        this.queuesURL = String.format(QUEUES_URL_PATTERN, baseUrl, vhostName);
        this.restTemplate = restTemplate;
        this.allQueuesUrl = String.format(ALL_QUEUES_URL_PATTERN, baseUrl);
        this.allExchangesUrl = String.format(ALL_EXCHANGES_URL_PATTERN, baseUrl);
    }

    /**
     * NOTE: This wiring means that the restTemplate from AdminQpidClientConfig#qpidRestTemplate() is used.
     * At the time of writing, this switches off host name verification in TLS.
     *
     * @param restTemplate
     * @param adminQpidClientProperties
     */

    @Autowired
    public AdminQpidClient(@Qualifier("qpidRestTemplate") RestTemplate restTemplate, AdminQpidClientProperties adminQpidClientProperties) {
        this(adminQpidClientProperties.getBaseUrl(), adminQpidClientProperties.getVhost(), restTemplate);
    }

    public boolean addBinding(String source, Binding binding) {
        AddBindingRequest request = new AddBindingRequest(binding);
        logger.info("Add binding {} from {} ", binding, source);
        String url = exchangesURL + "/" + source + "/bind";
        logger.debug("POSTint {} to URL {}", request, url);
        Boolean result = restTemplate.postForEntity(url, request, Boolean.class).getBody();
        return result.booleanValue();
    }

    public Queue createQueue(String name) {
        return createQueue(new CreateQueueRequest(name, MAX_TTL_15_MINUTES));
    }

    public Exchange createHeadersExchange(String name) {
        return createExchange(new CreateExchangeRequest(name, "headers"));
    }

    public Exchange createDirectExchange(String exchangeName) {
        return createExchange(new CreateExchangeRequest(exchangeName, "direct"));
    }

    private Queue createQueue(CreateQueueRequest request) {
        logger.info("Create queue {}", request.getName());
        String url = queuesURL + "/";
        logger.debug("POSTin {} to {}", request, url);
        Queue result = restTemplate.postForEntity(url, request, Queue.class).getBody();
        return result;
    }

    private Exchange createExchange(CreateExchangeRequest request) {
        logger.info("Create exchange {} of type {}", request.getName(), request.getType());
        String url = exchangesURL + "/";
        logger.debug("POSTing {} to {}", request, url);
        ResponseEntity<Exchange> response = restTemplate.postForEntity(url, request, Exchange.class);
        return response.getBody();
    }

    public boolean queueExists(String queueName) {
        return getQueue(queueName) != null;
    }

    public Queue getQueue(String queueName) {
        try {
            String url = queuesURL + "/" + queueName;
            logger.debug("GETting from {}", url);
            return restTemplate.getForEntity(url, Queue.class).getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    public Exchange getExchange(String exchangeName) {
        try {
            String url = exchangesURL + "/" + exchangeName;
            logger.debug("GETting from {}", url);
            return restTemplate.getForEntity(url, Exchange.class).getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    public boolean exchangeExists(String exchangeName) {
        return getExchange(exchangeName) != null;
    }


    public List<Queue> getAllQueues() throws JsonProcessingException {
        ResponseEntity<List<Queue>> allQueuesResponse = restTemplate.exchange(
                allQueuesUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });
        return allQueuesResponse.getBody();
    }

    public List<Exchange> getAllExchanges() throws JsonProcessingException {
        ResponseEntity<List<Exchange>> allExchangesResponse = restTemplate.exchange(
                allExchangesUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });
        return allExchangesResponse.getBody();
    }
}