package no.vegvesen.ixn.federation.serviceproviderclient.command.capabilities;

import picocli.CommandLine.*;
import java.util.concurrent.Callable;

@Command(name = "delete", description = "Delete a service provider capability",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
        """
                Example:\n
                serviceproviderclient capabilities delete a4154ea0-77aa-4f04-9e3f-c325430fd2be
                """
})
public class DeleteServiceProviderCapability implements Callable<Integer> {

    @ParentCommand
    CapabilitiesCommand parentCommand;

    @Parameters(index = "0", description = "The ID of the capability to delete")
    String capabilityId;

    @Override
    public Integer call() {
        parentCommand.getParent().createClient().deleteCapability(capabilityId);
        System.out.printf("Capability %s deleted successfully%n", capabilityId);
        return 0;
    }
}
