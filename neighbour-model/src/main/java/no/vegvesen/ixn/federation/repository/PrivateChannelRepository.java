package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.PrivateChannel;
import no.vegvesen.ixn.federation.model.PrivateChannelStatus;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.UUID;

public interface PrivateChannelRepository extends CrudRepository<PrivateChannel, Integer> {

    List<PrivateChannel> findAllByServiceProviderName(String serviceProviderName);

    List<PrivateChannel> findAllByStatusAndServiceProviderName(PrivateChannelStatus privateChannelStatus, String serviceProviderName);

    PrivateChannel findByServiceProviderNameAndUuidAndStatusIsNot(String serviceProviderName, String uuid, PrivateChannelStatus status);

    PrivateChannel findByServiceProviderNameAndUuid(String serviceProviderName, String uuid);

    List<PrivateChannel> findAllByPeerName(String peerName);

    long countByServiceProviderNameAndStatus(String serviceProviderName, PrivateChannelStatus status);

    long countByPeerNameAndStatus(String peerName, PrivateChannelStatus status);
}
