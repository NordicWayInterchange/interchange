package no.vegvesen.ixn.federation.serviceproviderclient.command.capabilities;

import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClientApplication;

import picocli.CommandLine.*;

@Command(name = "capabilities",
        description="list, add or delete capabilities for the current service provider",
        subcommands = {
                GetServiceProviderCapabilities.class,
                AddServiceProviderCapability.class,
                DeleteServiceProviderCapability.class,
                FetchMatchingCapabilities.class
        },
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0"
)
public class CapabilitiesCommand {

    @ParentCommand
    ServiceProviderClientApplication parentCommand;

    public ServiceProviderClientApplication getParent() {
        return parentCommand;
    }
}
