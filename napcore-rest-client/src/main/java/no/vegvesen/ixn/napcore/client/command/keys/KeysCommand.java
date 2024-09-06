package no.vegvesen.ixn.napcore.client.command.keys;

import no.vegvesen.ixn.napcore.client.NapRestClientApplication;
import static picocli.CommandLine.*;

@Command(
        name = "keys",
        subcommands = {
                CreateKeys.class
        },
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class KeysCommand {

    @ParentCommand
    NapRestClientApplication parentCommand;

    public NapRestClientApplication getParentCommand(){
        return parentCommand;
    }
}
