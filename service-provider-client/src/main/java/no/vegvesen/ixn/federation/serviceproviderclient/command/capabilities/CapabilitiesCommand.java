package no.vegvesen.ixn.federation.serviceproviderclient.command.capabilities;

import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClientApplication;

import picocli.CommandLine.*;

@Command(name = "capabilities",
        description="List, add or delete capabilities for the current Service Provider",
        subcommands = {
                GetServiceProviderCapabilities.class,
                AddServiceProviderCapability.class,
                DeleteServiceProviderCapability.class,
                FetchMatchingCapabilities.class
        },
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class CapabilitiesCommand {

    @ParentCommand
    ServiceProviderClientApplication parentCommand;

    public ServiceProviderClientApplication getParent() {
        return parentCommand;
    }
}
