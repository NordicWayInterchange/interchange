package no.vegvesen.ixn.napcore.client.command.privatechannels;


import no.vegvesen.ixn.napcore.client.NapRestClientApplication;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.PropertiesDefaultProvider;

@Command(
        name="privatechannels",
        description = "Get, add, list or delete private channels",
        subcommands = {
                AddPrivateChannel.class,
                DeletePrivateChannel.class,
                GetPeerPrivateChannels.class,
                GetPrivateChannels.class,
                GetPrivateChannel.class
        },
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true)

public class PrivatechannelsCommand {

    @ParentCommand
    NapRestClientApplication parentCommand;

    public NapRestClientApplication getParentCommand(){
        return parentCommand;
    }
}
