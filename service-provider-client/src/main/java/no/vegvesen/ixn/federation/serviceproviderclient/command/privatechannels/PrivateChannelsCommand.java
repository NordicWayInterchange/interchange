package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels;

import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClientApplication;
import no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.peers.PeersCommand;
import picocli.CommandLine.*;

@Command(
        name = "privatechannels",
        description = "Manage private channels for a Service Provider",
        subcommands = {
                GetPrivateChannels.class,
                GetPrivateChannel.class,
                AddPrivateChannel.class,
                DeletePrivateChannel.class,
                PeersCommand.class
        },
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class PrivateChannelsCommand {

    @ParentCommand
    ServiceProviderClientApplication parent;

    public ServiceProviderClientApplication getParent() {
        return parent;
    }
}