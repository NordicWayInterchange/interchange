package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels;

import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClientApplication;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(
        name = "privatechannels",
        description = "Manage private channels for a Service Provider",
        subcommands = {
                GetPrivateChannels.class,
                GetPrivateChannel.class,
                AddPrivateChannel.class,
                DeletePrivateChannel.class,
                GetPeerPrivateChannels.class
        },
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class PrivateChannelsCommand {

    @ParentCommand
    ServiceProviderClientApplication parent;


    public ServiceProviderClientApplication getParent() {
        return parent;
    }
}