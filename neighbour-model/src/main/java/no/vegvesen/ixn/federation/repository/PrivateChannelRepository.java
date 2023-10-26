package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.PrivateChannel;
import no.vegvesen.ixn.federation.model.PrivateChannelStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PrivateChannelRepository extends CrudRepository<PrivateChannel, Integer> {
    List<PrivateChannel> findAllByServiceProviderName(String serviceProviderName);
    List<PrivateChannel> findAllByStatus(PrivateChannelStatus privateChannelStatus);
    PrivateChannel findByServiceProviderNameAndId(String serviceProviderName, Integer Id);
    List<PrivateChannel> findAll();
    List<PrivateChannel> findAllByServiceProviderNameAndStatus(String serviceProviderName, PrivateChannelStatus status);
    List<PrivateChannel> findAllByPeerNameAndStatus(String peerName, PrivateChannelStatus status);
}
