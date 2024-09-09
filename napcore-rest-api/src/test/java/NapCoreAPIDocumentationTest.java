import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.capability.DenmApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;
import no.vegvesen.ixn.napcore.model.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


public class NapCoreAPIDocumentationTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void addNapSubscriptionRequestTest() throws JsonProcessingException {
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
                "originatingCountry = 'SE' and messageType = 'DENM' and quadTree like '%,12003%'");

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(subscriptionRequest));
    }

    @Test
    public void addNapSubscriptionResponseTest() throws JsonProcessingException {
        SubscriptionEndpoint endpoint = new SubscriptionEndpoint(
                "my-host",
                5671,
                "my-source",
                0,
                0
        );

        Subscription subscription = new Subscription(
                UUID.randomUUID().toString(),
                SubscriptionStatus.CREATED,
                "messageType = 'DENM'",
                new HashSet<>(Collections.singleton(endpoint)),
                LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond()
        );

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(subscription));
    }

    @Test
    public void napSubscriptionCapabilityResponse() throws JsonProcessingException {
        Capability capability = new Capability(
                new DenmApplicationApi(
                        "ID0001,",
                        "ID0001:0001",
                        "NO",
                        "DENM:001",
                        List.of("123123"),
                        List.of(1,2,3)
                ),
                new MetadataApi()
        );
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(Arrays.asList(capability)));
    }

    @Test
    public void testCertificateResponse() throws IOException {
        CertificateSignResponse response = new ObjectMapper().readValue(Paths.get("src","test","resources","certChainResponse.json").toFile(), CertificateSignResponse.class);
        assertThat(response.getChain()).hasSize(3);

        List<String> decoded = response.getChain().stream().map(s -> new String(Base64.getDecoder().decode(s))).collect(Collectors.toList());
        assertThat(decoded).allMatch(s -> s.startsWith("-----BEGIN CERTIFICATE-----\n")).allMatch(s -> s.endsWith("-----END CERTIFICATE-----\n"));

    }

}
