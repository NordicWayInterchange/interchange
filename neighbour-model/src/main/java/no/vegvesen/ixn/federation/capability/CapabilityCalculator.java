package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Capability;
import no.vegvesen.ixn.federation.model.ServiceProvider;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CapabilityCalculator {

    public static Set<Capability> allServiceProviderCapabilities(Iterable<ServiceProvider> serviceProviders) {
        Set<Capability> localCapabilities = new HashSet<>();
        for (ServiceProvider serviceProvider : serviceProviders) {
            Set<Capability> serviceProviderCapabilities = serviceProvider.getCapabilities().getCapabilities();
            localCapabilities.addAll(serviceProviderCapabilities);
        }
        return localCapabilities;
    }

    public static Set<Capability> allCreatedServiceProviderCapabilities(Iterable<ServiceProvider> serviceProviders) {
        Set<Capability> localCapabilities = new HashSet<>();
        for (ServiceProvider serviceProvider : serviceProviders) {
            Set<Capability> serviceProviderCapabilities = serviceProvider.getCapabilities().getCreatedCapabilities();
            localCapabilities.addAll(serviceProviderCapabilities);
        }
        return localCapabilities;
    }

    public static LocalDateTime calculateLastUpdatedCapabilities(List<ServiceProvider> serviceProviders) {
        LocalDateTime result = null;
        for (ServiceProvider serviceProvider : serviceProviders) {
            Capabilities capabilities = serviceProvider.getCapabilities();
            Optional<LocalDateTime> lastUpdated = capabilities.getLastUpdated();
            if (lastUpdated.isPresent()) {
                if (result == null || lastUpdated.get().isAfter(result)) {
                    result = lastUpdated.get();
                }
            }
        }
        return result;
    }

    public static Optional<LocalDateTime> calculateLastUpdatedCapabilitiesOptional(List<ServiceProvider> serviceProviders) {
        return Optional.ofNullable(calculateLastUpdatedCapabilities(serviceProviders));
    }
}
