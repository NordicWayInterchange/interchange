package no.vegvesen.ixn.federation.serviceproviderclient.command.deliveries;

import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClientApplication;

import no.vegvesen.ixn.federation.serviceproviderclient.command.deliveries.dlqueue.DlqCommand;
import picocli.CommandLine.*;

@Command(
        name = "deliveries",
        description = "get, add, list, send, listen to a dlq or delete deliveries for a service provider",
        subcommands = {
                ListDeliveries.class,
                GetDelivery.class,
                AddDeliveries.class,
                DeleteDelivery.class,
                FetchMatchingDeliveryCapabilities.class,
                DlqCommand.class,
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
