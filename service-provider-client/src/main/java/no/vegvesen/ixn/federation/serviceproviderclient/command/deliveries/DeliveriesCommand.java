package no.vegvesen.ixn.federation.serviceproviderclient.command.deliveries;


import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClientApplication;

import picocli.CommandLine.*;

@Command(
        name = "deliveries",
        description = "Get, add list or delete deliveries for a Service Provider",
        subcommands = {
                ListDeliveries.class,
                GetDelivery.class,
                AddDeliveries.class,
                DeleteDelivery.class,
                Send.class
        },
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class DeliveriesCommand {

    @ParentCommand
    ServiceProviderClientApplication parent;


    public ServiceProviderClientApplication getParent() {
        return parent;
    }
}
