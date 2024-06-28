package no.vegvesen.ixn.napcore.client.command.capabilities;

import no.vegvesen.ixn.napcore.client.NapRestClientApplication;
import static picocli.CommandLine.*;

@Command(name = "capabilities",
        description="List, get, add or delete capabilities for the current Service Provider",
        subcommands = {
                AddNapCapability.class,
                GetNapCapability.class,
                GetNapCapabilities.class,
                DeleteNapCapability.class
        },
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class CapabilitiesCommand {

    @ParentCommand
    NapRestClientApplication parentCommand;

    public NapRestClientApplication getParentCommand(){
        return parentCommand;
    }
}
