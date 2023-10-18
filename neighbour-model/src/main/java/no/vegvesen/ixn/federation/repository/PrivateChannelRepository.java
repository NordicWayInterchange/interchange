package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.PrivateChannel;
import org.springframework.data.repository.CrudRepository;

public interface PrivateChannelRepository extends CrudRepository<PrivateChannel, Integer> {
    boolean existsByPeerName(String Peername);
    PrivateChannel findByPeerName(String Peername);
}
