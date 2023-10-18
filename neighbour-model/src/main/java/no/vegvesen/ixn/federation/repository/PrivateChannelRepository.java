package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.PrivateChannel;
import no.vegvesen.ixn.federation.model.PrivateChannelStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PrivateChannelRepository extends CrudRepository<PrivateChannel, Integer> {
    boolean existsByPeerName(String Peername);
    PrivateChannel findByPeerName(String Peername);
    List<PrivateChannel> findAllByStatus(PrivateChannelStatus privateChannelStatus);
    List<PrivateChannel> findAll();
}
