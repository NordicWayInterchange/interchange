package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.model.PrivateChannel;
import no.vegvesen.ixn.federation.model.PrivateChannelStatus;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import javax.transaction.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Transactional
public class PrivateChannelTests {

    @Autowired
    OnboardRestController restController;

    @MockBean
    PrivateChannelRepository repository;

    @MockBean
    private CertService certService;

    @Test
    public void testAddingMultipleChannels(){
        String serviceProviderName = "king_olaf.bouvetinterchange.eu";
        PrivateChannelApi privateChannel_1 = new PrivateChannelApi("king_gustaf.bouvetinterchange.eu");
        AddPrivateChannelRequest request = new AddPrivateChannelRequest(serviceProviderName,List.of(privateChannel_1, privateChannel_1, privateChannel_1));
        PrivateChannel savedChannel = new PrivateChannel(privateChannel_1.getPeerName(), PrivateChannelStatus.REQUESTED, serviceProviderName);

        when(repository.findAllByServiceProviderName(serviceProviderName)).thenReturn(List.of(savedChannel, savedChannel, savedChannel));
        when(repository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        AddPrivateChannelResponse response = restController.addPrivateChannel(serviceProviderName, request);

        assertThat(response.getPrivateChannels().size()).isEqualTo(3);
        assertThat(restController.getPrivateChannels(serviceProviderName).getPrivateChannels().size()).isEqualTo(3);

        verify(certService, times(2)).checkIfCommonNameMatchesNameInApiObject(any());
        verify(repository, times(3)).save(any());
    }

    @Test
    public void testAddingInvalidChannel(){

        String serviceProviderName = "king_olaf.bouvetinterchange.eu";
        AddPrivateChannelRequest request = new AddPrivateChannelRequest(serviceProviderName,List.of());

        assertThrows(RuntimeException.class, () -> restController.addPrivateChannel(serviceProviderName, new AddPrivateChannelRequest(serviceProviderName, null)));
        assertThrows(RuntimeException.class, () -> restController.addPrivateChannel(serviceProviderName, null));
        assertThrows(RuntimeException.class, () -> restController.addPrivateChannel(serviceProviderName, request));

        verify(certService, times(3)).checkIfCommonNameMatchesNameInApiObject(any());
        verify(repository, times(0)).save(any());
    }

    @Test
    public void testAddingAndDeletingChannel(){
        String serviceProviderName = "king_olaf.bouvetinterchange.eu";
        PrivateChannelApi privateChannel_1 = new PrivateChannelApi("king_gustaf.bouvetinterchange.eu");
        AddPrivateChannelRequest request = new AddPrivateChannelRequest(serviceProviderName,List.of(privateChannel_1));
        PrivateChannel savedPrivateChannel = new PrivateChannel(privateChannel_1.getPeerName(), PrivateChannelStatus.REQUESTED, serviceProviderName);
        savedPrivateChannel.setId(2);

        when(repository.save(any())).thenAnswer(i-> i.getArguments()[0]);
        when(repository.findByServiceProviderNameAndId(any(), any())).thenReturn(savedPrivateChannel);

        restController.addPrivateChannel(serviceProviderName, request);
        restController.deletePrivateChannel(serviceProviderName, savedPrivateChannel.getId().toString());

        when(repository.findAllByServiceProviderName(serviceProviderName)).thenReturn(List.of(savedPrivateChannel));
        assertThat(restController.getPrivateChannels(serviceProviderName).getPrivateChannels().get(0).getStatus()).isEqualTo(PrivateChannelStatusApi.TEAR_DOWN);

        verify(certService, times(3)).checkIfCommonNameMatchesNameInApiObject(any());
        verify(repository, times(2)).save(any());
    }
    @Test
    public void testDeletingNonExistentChannel(){
        String serviceProviderName = "king_olaf.bouvetinterchange.eu";
        PrivateChannelApi privateChannel_1 = new PrivateChannelApi("king_gustaf.bouvetinterchange.eu");
        AddPrivateChannelRequest request = new AddPrivateChannelRequest(List.of(privateChannel_1));

        PrivateChannel savedPrivateChannel = new PrivateChannel(privateChannel_1.getPeerName(), PrivateChannelStatus.REQUESTED, serviceProviderName);
        savedPrivateChannel.setId(2);

        when(repository.save(any())).thenAnswer(i-> i.getArguments()[0]);
        when(repository.findAllByServiceProviderName(serviceProviderName)).thenReturn(List.of(savedPrivateChannel));
        restController.addPrivateChannel(serviceProviderName,request);

        assertThrows(RuntimeException.class, () -> restController.deletePrivateChannel(serviceProviderName, ""));
        assertThrows(RuntimeException.class, () -> restController.deletePrivateChannel(serviceProviderName, "88"));

        verify(certService, times(3)).checkIfCommonNameMatchesNameInApiObject(any());
        verify(repository, times(1)).findByServiceProviderNameAndId(any(), any());
    }
    @Test
    public void testGettingOneChannel(){
        String serviceProviderName = "king_olaf.bouvetinterchange.eu";
        PrivateChannelApi privateChannel_1 = new PrivateChannelApi("king_gustaf.bouvetinterchange.eu");
        AddPrivateChannelRequest request = new AddPrivateChannelRequest(serviceProviderName,List.of(privateChannel_1));
        PrivateChannel savedPrivateChannel = new PrivateChannel(privateChannel_1.getPeerName(), PrivateChannelStatus.REQUESTED, serviceProviderName);
        savedPrivateChannel.setId(2);

        when(repository.save(any())).thenAnswer(i->i.getArguments()[0]);
        when(repository.findByServiceProviderNameAndIdAndStatusIsNot(any(),any(), any())).thenReturn(savedPrivateChannel);

        restController.addPrivateChannel(serviceProviderName, request);

        assertThat(restController.getPrivateChannel(serviceProviderName, savedPrivateChannel.getId().toString())).isNotNull();

        verify(certService, times(2)).checkIfCommonNameMatchesNameInApiObject(any());
        verify(repository, times(1)).save(any());
        verify(repository, times(1)).findByServiceProviderNameAndIdAndStatusIsNot(any(),any(),any());
    }

    @Test
    public void testGettingNonExistentChannel(){
        String serviceProviderName = "king_olaf.bouvetinterchange.eu";
        PrivateChannelApi privateChannel_1 = new PrivateChannelApi("king_gustaf.bouvetinterchange.eu");
        AddPrivateChannelRequest request = new AddPrivateChannelRequest(serviceProviderName,List.of(privateChannel_1));
        PrivateChannel savedPrivateChannel = new PrivateChannel(privateChannel_1.getPeerName(), PrivateChannelStatus.REQUESTED, serviceProviderName);
        savedPrivateChannel.setId(2);

        when(repository.save(any())).thenAnswer(i->i.getArguments()[0]);
        when(repository.findByServiceProviderNameAndIdAndStatusIsNot(serviceProviderName,2, PrivateChannelStatus.REQUESTED)).thenReturn(savedPrivateChannel);

        restController.addPrivateChannel(serviceProviderName, request);

        assertThrows(RuntimeException.class, ()-> restController.getPrivateChannel(serviceProviderName, Integer.valueOf(3).toString()));
        assertThrows(RuntimeException.class, () -> restController.getPrivateChannel(serviceProviderName, ""));

        verify(certService, times(3)).checkIfCommonNameMatchesNameInApiObject(any());
        verify(repository, times(1)).save(any());
    }

    @Test
    public void testGetPeerPrivateChannels(){
        String serviceProviderName = "king_olaf.bouvetinterchange.eu";
        PrivateChannelApi privateChannel_1 = new PrivateChannelApi("king_gustaf.bouvetinterchange.eu");
        AddPrivateChannelRequest request = new AddPrivateChannelRequest(serviceProviderName,List.of(privateChannel_1));

        when(repository.save(any())).thenAnswer(i->i.getArguments()[0]);
        when(repository.findAllByPeerName(serviceProviderName)).thenReturn(List.of());
        restController.addPrivateChannel(serviceProviderName, request);

        assertThat(restController.getPeerPrivateChannels(serviceProviderName).getPrivateChannels().size()).isEqualTo(0);
        verify(certService, times(2)).checkIfCommonNameMatchesNameInApiObject(any());
        verify(repository, times(1)).save(any());
    }
}
