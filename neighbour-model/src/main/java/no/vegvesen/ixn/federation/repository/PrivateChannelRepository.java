package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.PrivateChannel;
import no.vegvesen.ixn.federation.model.PrivateChannelStatus;
import org.apache.qpid.server.model.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface PrivateChannelRepository extends CrudRepository<PrivateChannel, Integer> {

    List<PrivateChannel> findAllByServiceProviderName(String serviceProviderName);

    List<PrivateChannel> findAllByStatusAndServiceProviderName(PrivateChannelStatus privateChannelStatus, String serviceProviderName);

    PrivateChannel findByServiceProviderNameAndUuidAndStatusIsNot(String serviceProviderName, String uuid, PrivateChannelStatus status);

    PrivateChannel findByServiceProviderNameAndUuid(String serviceProviderName, String uuid);

    @Query("from PrivateChannel pc join pc.peers ps where ps.name=:peerName")
    List<PrivateChannel> findAllByPeerName(@Param(name = "peerName") String peerName);

    @Query("from PrivateChannel pc join pc.peers ps where ps.name=:peerName and pc.status=:status")
    List<PrivateChannel> findAllByPeerNameAndStatus(@Param(name = "peerName") String peerName, @Param(name = "status") PrivateChannelStatus status);

    long countByServiceProviderNameAndStatus(String serviceProviderName, PrivateChannelStatus status);

}
