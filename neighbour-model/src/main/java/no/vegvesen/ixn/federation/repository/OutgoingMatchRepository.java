package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.OutgoingMatch;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutgoingMatchRepository extends CrudRepository<OutgoingMatch, Integer> {

    List<OutgoingMatch> findAll();

    List<OutgoingMatch> findAllByServiceProviderName(String serviceProviderName);

    List<OutgoingMatch> findAllByLocalDelivery_Id(Integer id);

    List<OutgoingMatch> findAllByLocalDelivery_ExchangeName(String deliveryExchangeName);

    OutgoingMatch findByLocalDelivery_Id(Integer id);

    List<OutgoingMatch> findAllByCapability_Id(Integer id);

    OutgoingMatch findByCapability_Id(Integer id);

    OutgoingMatch findByCapability_IdAndLocalDelivery_Id(Integer capabilityId, Integer localDeliveryId);
}
