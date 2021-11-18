package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.model.Capability;
import no.vegvesen.ixn.federation.model.ServiceProvider;

import java.util.HashSet;
import java.util.Set;

public class CapabilityCalculator {
    public static Set<Capability> calculateSelfCapabilities(Iterable<ServiceProvider> serviceProviders) {
        //SelfService.logger.info("Calculating Self capabilities...");
        Set<Capability> localCapabilities = new HashSet<>();

        for (ServiceProvider serviceProvider : serviceProviders) {
            //SelfService.logger.info("Service provider name: {}", serviceProvider.getName());
            Set<Capability> serviceProviderCapabilities = serviceProvider.getCapabilities().getCapabilities();
            //SelfService.logger.info("Service Provider capabilities: {}", serviceProviderCapabilities.toString());
            localCapabilities.addAll(serviceProviderCapabilities);
        }
        //SelfService.logger.info("Calculated Self capabilities: {}", localCapabilities);
        return localCapabilities;
    }
}
