package no.vegvesen.ixn.napcore.client.command.keys;

import no.vegvesen.ixn.napcore.client.NapRestClientApplication;
import picocli.CommandLine;

@CommandLine.Command(
        name = "keys",
        subcommands = {
                CreateKeys.class
        },
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class KeysCommand {

    @CommandLine.ParentCommand
    NapRestClientApplication parentCommand;

    public NapRestClientApplication getParentCommand(){
        return parentCommand;
    }
}
