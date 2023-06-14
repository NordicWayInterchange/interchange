package no.vegvesen.ixn.napcore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.cert.CertSigner;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.model.LocalEndpoint;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.napcore.model.Subscription;
import no.vegvesen.ixn.napcore.properties.NapCoreProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;

@WebMvcTest(controllers = NapRestController.class)
@ContextConfiguration(classes = {CertService.class, NapRestController.class, InterchangeNodeProperties.class})
public class NapRestControllerTest {

    private MockMvc mockMvc;

    @MockBean
    ServiceProviderRepository serviceProviderRepository;

    @MockBean
    private NeighbourRepository neighbourRepository;

    @MockBean
    private NapCoreProperties napCoreProperties;

    @MockBean
    private CertSigner certSigner;

    @Autowired
    private NapRestController restController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(restController)
                .setMessageConverters(NapStrictWebConfig.strictJsonMessageConverter())
                .setControllerAdvice(NapServerErrorAdvice.class)
                .build();
    }

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Disabled
    public void testReturningListOfSubscriptionsInsteadOfListObject() throws JsonProcessingException {
        ServiceProvider serviceProvider = new ServiceProvider("sp-1");

        LocalEndpoint endpoint = new LocalEndpoint(
                "my-source",
                "my-host",
                5671,
                0,
                0
        );

        LocalSubscription subscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "originatingCountry = 'NO'", "sp-1");
        subscription.setId(1);
        subscription.setLocalEndpoints(Collections.singleton(endpoint));

        serviceProvider.addLocalSubscription(subscription);

        when(serviceProviderRepository.findByName(serviceProvider.getName())).thenReturn(serviceProvider);
        List<Subscription> response = restController.getSubscriptions(serviceProvider.getName());
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    @Test
    public void postingNapCsrForSigningReturnsSignedCertificate() {

    }

    @Test
    public void postingNapSubscriptionReturnsStatusOk() {

    }

    @Test
    public void postingNapSubscriptionWithExtraFieldsReturnsStatusBadRequest() {

    }

    @Test
    public void postingInvalidNapSubscriptionRequestReturnsStatusBadRequest() {

    }

    @Test
    public void deletingNapSubscriptionReturnsNoContent() {

    }

    @Test
    public void deletingNonExistingNapSubscriptionReturnsStatusNotFound() {

    }
}
