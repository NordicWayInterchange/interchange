package no.vegvesen.ixn.serviceprovider.client.command.deliveries;

import no.vegvesen.ixn.serviceprovider.client.OnboardRestClientApplication;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "deliveries",
        description = "Get, add list or delete deliveries for a Service Provider"
)
public class DeliveriesCommand {

    @CommandLine.ParentCommand
    OnboardRestClientApplication parent;


    public OnboardRestClientApplication getParent() {
        return parent;
    }
}
