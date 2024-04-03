package no.vegvesen.ixn.serviceprovider.client.command.capabilities;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(name = "delete", description = "Delete a service provider capability")
public class DeleteServiceProviderCapability implements Callable<Integer> {

    @ParentCommand
    CapabilitiesCommand parentCommand;

    @Parameters(index = "0", description = "The ID of the capability to delete")
    String capabilityId;

    @Override
    public Integer call() {
        parentCommand.getParentCommand().createClient().deleteCapability(capabilityId);
        System.out.printf("Capability %s deleted successfully%n", capabilityId);
        return 0;
    }
}
