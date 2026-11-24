package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.PrivateChannel;
import no.vegvesen.ixn.federation.model.PrivateChannelStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PrivateChannelRepository extends CrudRepository<PrivateChannel, Integer> {

    List<PrivateChannel> findAll();

    List<PrivateChannel> findAllByServiceProviderName(String serviceProviderName);

    List<PrivateChannel> findAllByStatusAndServiceProviderName(PrivateChannelStatus privateChannelStatus, String serviceProviderName);

    PrivateChannel findByServiceProviderNameAndUuidAndStatusIsNot(String serviceProviderName, String uuid, PrivateChannelStatus status);

    PrivateChannel findByServiceProviderNameAndUuidAndStatus(String serviceProviderName, String Uuid, PrivateChannelStatus status);

    PrivateChannel findByServiceProviderNameAndUuid(String serviceProviderName, String uuid);

    @Query("select pc from PrivateChannel pc join pc.peers ps where ps.name=:peerName")
    List<PrivateChannel> findAllByPeerName(@Param("peerName") String peerName);

    @Query(" select pc from PrivateChannel pc join pc.peers ps where ps.name=:peerName and pc.status=:status")
    List<PrivateChannel> findAllByPeerNameAndStatus(@Param("peerName") String peerName, @Param("status") PrivateChannelStatus status);

    @Query("select pc from PrivateChannel pc join pc.peers ps where ps.name=:peerName and pc.uuid=:uuid")
    PrivateChannel findByUuidAndPeerName(@Param("uuid") String uuid, @Param("peerName") String peerName);

    long countByServiceProviderNameAndStatus(String serviceProviderName, PrivateChannelStatus status);

}
