package no.vegvesen.ixn.napcore;

import no.vegvesen.ixn.cert.CertSigner;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.LocalEndpoint;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.napcore.properties.NapCoreProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NapRestController.class)
@ContextConfiguration(classes = {NapRestController.class, InterchangeNodeProperties.class, NapRestControllerTest.NapCorePropertiesCreator.class})
public class NapRestControllerTest {

    public static final String NODE_NAME = "interchangenode";
    public static final String NAP_USER_NAME = "napcn";
    private MockMvc mockMvc;

    @MockBean
    ServiceProviderRepository serviceProviderRepository;

    @MockBean
    private NeighbourRepository neighbourRepository;

    private NapCoreProperties napCoreProperties;

    @MockBean
    private CertSigner certSigner;

    @MockBean
    private CertService certService;

    @Autowired
    private NapRestController restController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(restController)
                .setMessageConverters(NapStrictWebConfig.strictJsonMessageConverter())
                .setControllerAdvice(NapServerErrorAdvice.class)
                .alwaysExpect(content().contentType(MediaType.APPLICATION_JSON))
                .build();
    }


    @Test
    @DisplayName("Get existing local subscription")
    public void getLocalSubscriptions() throws Exception {
        String serviceProviderName = "sp-1";
        ServiceProvider serviceProvider = new ServiceProvider(
                1,
                serviceProviderName,
                new Capabilities(),
                Collections.singleton(
                        new LocalSubscription(
                                1,
                                LocalSubscriptionStatus.REQUESTED,
                                "originatingCountry = 'NO'",
                                serviceProviderName,
                                Collections.emptySet(),
                                Collections.singleton(
                                        new LocalEndpoint(
                                                "my-source",
                                                "my-host",
                                                5671,
                                                0,
                                                0
                                        )
                                )
                        )
                ),
                null
        );
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        when(serviceProviderRepository.findByName(serviceProvider.getName())).thenReturn(serviceProvider);

        mockMvc.perform(
                get(String.format("/nap/%s/subscriptions",serviceProviderName))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*",isA(ArrayList.class)))
                .andExpect(jsonPath("$.*",hasSize(1)));
        verify(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        verify(serviceProviderRepository,times(1)).findByName(serviceProviderName);
    }

    @Test
    @DisplayName("Getting a non-existing Service Provider returns empty list")
    public void getNonexistingServiceProvider() throws Exception {
        String serviceProviderName = "sp-1";
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        when(serviceProviderRepository.findByName(serviceProviderName)).thenReturn(null);

        mockMvc.perform(
                        get(String.format("/nap/%s/subscriptions",serviceProviderName))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*",isA(ArrayList.class)))
                .andExpect(jsonPath("$.*",hasSize(0)));
        verify(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        verify(serviceProviderRepository,times(1)).findByName(serviceProviderName);
    }

    @Test
    @DisplayName("Get the details of a local subscription")
    public void getSingleLocalSubscription() throws Exception {
        String serviceProviderName = "sp-1";
        ServiceProvider serviceProvider = new ServiceProvider(
                1,
                serviceProviderName,
                new Capabilities(),
                Collections.singleton(
                        new LocalSubscription(
                                1,
                                LocalSubscriptionStatus.REQUESTED,
                                "originatingCountry = 'NO'",
                                serviceProviderName,
                                Collections.emptySet(),
                                Collections.singleton(
                                        new LocalEndpoint(
                                                "my-source",
                                                "my-host",
                                                5671,
                                                0,
                                                0
                                        )
                                )
                        )
                ),
                null
        );
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        when(serviceProviderRepository.findByName(serviceProviderName)).thenReturn(serviceProvider);
        mockMvc.perform(
                        get(String.format("/nap/%s/subscriptions/%d",serviceProviderName,serviceProvider.getId()))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id",is(1)));
        verify(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        verify(serviceProviderRepository).findByName(serviceProviderName);
    }

    @Test
    @DisplayName("Get single, non-existing local subscription")
    public void getNonExistingSubscription() throws Exception {
        String serviceProviderName = "sp-1";
        ServiceProvider serviceProvider = new ServiceProvider(
                1,
                serviceProviderName,
                new Capabilities(),
                Collections.emptySet(),
                null
        );
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        when(serviceProviderRepository.findByName(serviceProviderName)).thenReturn(serviceProvider);
        mockMvc.perform(
                        get(String.format("/nap/%s/subscriptions/%d",serviceProviderName,serviceProvider.getId()))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
        verify(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        verify(serviceProviderRepository).findByName(serviceProviderName);
    }

    @Test
    public void postingNapSubscriptionWithExtraFieldsReturnsStatusBadRequest() throws Exception {
        String request = """
                {
                "selector": "originatingCountry='NO'",
                "extraField": "extra"
                }
                """;
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        mockMvc.perform(
                post(String.format("/nap/%s/subscriptions", "actor"))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        ).andExpect(status().isBadRequest());
    }

    @Test
    public void postingInvalidNapSubscriptionRequestReturnsStatusBadRequest() throws Exception {
        String invalidRequest = "";
        String serviceProviderName = "actor";
        ServiceProvider serviceProvider = new ServiceProvider(
                1,
                serviceProviderName,
                new Capabilities(),
                Collections.emptySet(),
                null
        );
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        mockMvc.perform(
                post(String.format("/nap/%s/subscriptions", "actor"))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest)
        ).andExpect(status().isBadRequest());
    }

    /*
    @Test
    public void deletingNapSubscriptionReturnsNoContent() throws Exception {
        String serviceProviderName = "actor";
        ServiceProvider serviceProvider = new ServiceProvider(
                1,
                serviceProviderName,
                new Capabilities(),
                Collections.emptySet(),
                null
        );
        LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, "originatingCountry='NO'", "actor");
        localSubscription.setId(1);
        serviceProvider.addLocalSubscription(localSubscription);
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        when(serviceProviderRepository.findByName(anyString())).thenReturn(serviceProvider);
        when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);

        mockMvc.perform(
                delete(String.format("/nap/%s/subscriptions/%s", serviceProviderName, "1"))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)

        ).andExpect(status().isNoContent());
    }
    */

    @Test
    public void postDeliveryReturnsStatusOk(){}

    @Test
    public void postDeliveryWithExtraFieldsReturnsStatusBadRequest(){}

    @Test
    public void postDeliveryWithNullSelectorReturnsStatusBadrequest(){}

    @Test
    public void postingInvalidDeliveryRequestReturnsStatusBadRequest(){}

    @Test
    public void deletingNonExistingNapSubscriptionReturnsStatusNotFound() throws Exception{
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        mockMvc.perform(
                delete(String.format("/nap/%s/subscriptions/%s", "actor", "1"))
        ).andExpect(status().isNotFound());
    }

    @Configuration
    public static class NapCorePropertiesCreator {
        @Bean
        public NapCoreProperties napCoreProperties() {
            return new NapCoreProperties(
                    NODE_NAME,
                    NAP_USER_NAME
            );
        }
    }
}
