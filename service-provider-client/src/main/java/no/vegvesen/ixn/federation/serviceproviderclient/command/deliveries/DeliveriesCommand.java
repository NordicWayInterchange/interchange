package no.vegvesen.ixn.federation.serviceproviderclient.command.deliveries;

import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClientApplication;

import picocli.CommandLine.*;

@Command(
        name = "deliveries",
        description = "get, add, list, send, listen or delete deliveries for a service provider",
        subcommands = {
                ListDeliveries.class,
                GetDelivery.class,
                AddDeliveries.class,
                DeleteDelivery.class,
                FetchMatchingDeliveryCapabilities.class,
                Listen.class,
                Send.class
        },
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0"
)
public class DeliveriesCommand {

    @ParentCommand
    ServiceProviderClientApplication parent;

    public ServiceProviderClientApplication getParent() {
        return parent;
    }
}
