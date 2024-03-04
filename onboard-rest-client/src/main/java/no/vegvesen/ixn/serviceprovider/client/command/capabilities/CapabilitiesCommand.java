package no.vegvesen.ixn.serviceprovider.client.command.capabilities;

import no.vegvesen.ixn.serviceprovider.client.OnboardRestClientApplication;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "capabilities",
        description="List, add or delete capabilities for the current Service Provider",
        subcommands = {
                GetServiceProviderCapabilities.class,
                AddServiceProviderCapability.class,
                DeleteServiceProviderCapability.class
        },
	defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
	mixinStandardHelpOptions = true
	)
public class CapabilitiesCommand {

    @ParentCommand
    OnboardRestClientApplication parentCommand;


    public OnboardRestClientApplication getParentCommand() {
        return parentCommand;
    }

}
