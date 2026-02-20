package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels;

import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClientApplication;
import no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.peers.PeersCommand;
import picocli.CommandLine.*;

@Command(
        name = "privatechannels",
        description = "manage private channels for a service provider",
        subcommands = {
                GetPrivateChannels.class,
                GetPrivateChannel.class,
                AddPrivateChannel.class,
                DeletePrivateChannel.class,
                PeersCommand.class,
                Send.class,
                Listen.class
        },
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0"
)
public class PrivateChannelsCommand {

    @ParentCommand
    ServiceProviderClientApplication parent;

    public ServiceProviderClientApplication getParent() {
        return parent;
    }
}