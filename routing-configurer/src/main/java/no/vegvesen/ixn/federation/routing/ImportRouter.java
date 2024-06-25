package no.vegvesen.ixn.federation.routing;

import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityShard;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.federation.qpid.Exchange;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidDelta;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@ConfigurationPropertiesScan("no.vegvesen.ixn")
public class ImportRouter {

    private static Logger logger = LoggerFactory.getLogger(ImportRouter.class);

    private final ServiceProviderRepository serviceProviderRepository;

    private final QpidClient qpidClient;

    @Autowired
    public ImportRouter(ServiceProviderRepository serviceProviderRepository, QpidClient qpidClient) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.qpidClient = qpidClient;
    }

    public void setupImportedModel(QpidDelta delta) {

    }

    public Set<ServiceProvider> findImportedCapabilitiesByServiceProvider() {
        return serviceProviderRepository.findAll().stream().filter(ServiceProvider::hasImportedCapabilities).collect(Collectors.toSet());
    }

    public void setupImportedCapabilities(QpidDelta delta) {
        for (ServiceProvider serviceProvider : findImportedCapabilitiesByServiceProvider()) {
            Set<Capability> importedCapabilities = serviceProvider.getCapabilities().getCapabilitiesByStatus(CapabilityStatus.IMPORTED_CREATED);
            for (Capability capability : importedCapabilities) {
                setupCapabilityShards(capability.getShards(), delta);
                capability.setStatus(CapabilityStatus.CREATED);
            }
            serviceProviderRepository.save(serviceProvider);
        }
    }

    public void setupCapabilityShards(List<CapabilityShard> shards, QpidDelta delta) {
        for (CapabilityShard shard : shards) {
            if (!delta.exchangeExists(shard.getExchangeName())) {
                Exchange exchange = qpidClient.createHeadersExchange(shard.getExchangeName());
                delta.addExchange(exchange);
            }
        }
    }

    public Set<ServiceProvider> findImportedLocalSubscriptionsByServiceProvider() {
        return serviceProviderRepository.findAll().stream().filter(ServiceProvider::hasImportedLocalSubscriptions).collect(Collectors.toSet());
    }

}
