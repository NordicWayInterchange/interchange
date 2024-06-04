package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.DeliveryCapabilityMatch;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryCapabilityMatchRepository extends CrudRepository<DeliveryCapabilityMatch, Integer> {

    List<DeliveryCapabilityMatch> findAll();

    List<DeliveryCapabilityMatch> findAllByServiceProviderName(String serviceProviderName);

    List<DeliveryCapabilityMatch> findAllByLocalDelivery_Id(Integer id);

    List<DeliveryCapabilityMatch> findAllByLocalDelivery_ExchangeName(String deliveryExchangeName);

    DeliveryCapabilityMatch findByLocalDelivery_Id(Integer id);

    List<DeliveryCapabilityMatch> findAllByCapability_Id(Integer id);

    DeliveryCapabilityMatch findByCapability_Id(Integer id);

    DeliveryCapabilityMatch findByCapability_IdAndLocalDelivery_Id(Integer capabilityId, Integer localDeliveryId);
}
