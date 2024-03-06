package no.vegvesen.ixn.serviceprovider.client.command.privatechannel;

import no.vegvesen.ixn.serviceprovider.client.OnboardRestClientApplication;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(
        name = "privatechannels",
        description = "Manage private channels for a Service Provider",
        subcommands = {
                AddPrivateChannel.class
        },
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class PrivateChannelCommand {

    @ParentCommand
    OnboardRestClientApplication parent;


    public OnboardRestClientApplication getParent() {
        return parent;
    }
}
