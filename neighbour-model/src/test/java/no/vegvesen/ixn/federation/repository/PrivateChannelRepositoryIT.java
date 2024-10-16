package no.vegvesen.ixn.federation.repository;

import jakarta.transaction.Transactional;
import no.vegvesen.ixn.docker.PostgresContainerBase;
import no.vegvesen.ixn.federation.model.Peer;
import no.vegvesen.ixn.federation.model.PrivateChannel;
import no.vegvesen.ixn.federation.model.PrivateChannelStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class PrivateChannelRepositoryIT extends PostgresContainerBase {

    @Autowired
    private PrivateChannelRepository repository;

    @Test
    public void getPrivateChannelsByNameInPeersListAndStatus() {
        String serviceProviderName1 = "king_olav.bouvetinterchange.eu";
        String serviceProviderName2 = "king_gustaf.bouvetinterchange.eu";
        String peerName = "king_frederik.bouvetinterchange.eu";

        PrivateChannel privateChannel1 = new PrivateChannel(Collections.singleton(new Peer(peerName)), PrivateChannelStatus.CREATED, serviceProviderName1);
        PrivateChannel privateChannel2 = new PrivateChannel(Collections.singleton(new Peer(peerName)), PrivateChannelStatus.CREATED, serviceProviderName2);

        repository.saveAll(Arrays.asList(privateChannel1, privateChannel2));

        assertThat(repository.findAllByPeerNameAndStatus(peerName, PrivateChannelStatus.CREATED)).hasSize(2);
    }

    @Test
    public void getPrivateChannelsByNameInPeersListMatchingOneStatus() {
        String serviceProviderName1 = "king_olav.bouvetinterchange.eu";
        String serviceProviderName2 = "king_gustaf.bouvetinterchange.eu";
        String peerName = "king_frederik.bouvetinterchange.eu";

        PrivateChannel privateChannel1 = new PrivateChannel(Collections.singleton(new Peer(peerName)), PrivateChannelStatus.REQUESTED, serviceProviderName1);
        PrivateChannel privateChannel2 = new PrivateChannel(Collections.singleton(new Peer(peerName)), PrivateChannelStatus.CREATED, serviceProviderName2);

        repository.saveAll(Arrays.asList(privateChannel1, privateChannel2));

        assertThat(repository.findAllByPeerNameAndStatus(peerName, PrivateChannelStatus.CREATED)).hasSize(1);
    }

    @Test
    public void doNotGetPrivateChannelByPeerNameAndStatusWhenStatusIsWrong() {
        String serviceProviderName = "king_olav.bouvetinterchange.eu";
        String peerName = "king_frederik.bouvetinterchange.eu";

        PrivateChannel privateChannel = new PrivateChannel(Collections.singleton(new Peer(peerName)), PrivateChannelStatus.REQUESTED, serviceProviderName);

        repository.saveAll(Collections.singleton(privateChannel));

        assertThat(repository.findAllByPeerNameAndStatus(peerName, PrivateChannelStatus.CREATED)).hasSize(0);
    }

    @Test
    public void getPrivateChannelsByNameInPeersList() {
        String serviceProviderName1 = "king_olav.bouvetinterchange.eu";
        String serviceProviderName2 = "king_gustaf.bouvetinterchange.eu";
        String peerName = "king_frederik.bouvetinterchange.eu";

        PrivateChannel privateChannel1 = new PrivateChannel(Collections.singleton(new Peer(peerName)), PrivateChannelStatus.CREATED, serviceProviderName1);
        PrivateChannel privateChannel2 = new PrivateChannel(Collections.singleton(new Peer(peerName)), PrivateChannelStatus.CREATED, serviceProviderName2);

        repository.saveAll(Arrays.asList(privateChannel1, privateChannel2));

        assertThat(repository.findAllByPeerName(peerName)).hasSize(2);
    }
}
