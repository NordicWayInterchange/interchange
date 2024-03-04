package no.vegvesen.ixn.serviceprovider.client.command.capabilities;

import no.vegvesen.ixn.serviceprovider.client.OnboardRESTClient;
import no.vegvesen.ixn.serviceprovider.client.OnboardRestClientApplication;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "deletecapability", description = "Delete a service provider capability")
public class DeleteServiceProviderCapability implements Callable<Integer> {

    @CommandLine.ParentCommand
    OnboardRestClientApplication parentCommand;

    @CommandLine.Parameters(index = "0", description = "The ID of the capability to delete")
    String capabilityId;

    @Override
    public Integer call() {
        OnboardRESTClient client = parentCommand.createClient();
        client.deleteCapability(capabilityId);
        System.out.printf("Capability %s deleted successfully%n", capabilityId);
        return 0;
    }
}
