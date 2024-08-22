package no.vegvesen.ixn.federation.serviceproviderclient.command.capabilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderclient.OnboardRESTClient;
import no.vegvesen.ixn.serviceprovider.model.ListCapabilitiesResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(
        name = "list",
        description = "List the service provider capabilities",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class GetServiceProviderCapabilities implements Callable<Integer> {

    @ParentCommand
    CapabilitiesCommand parentCommand;

    @Override
    public Integer call() throws Exception {
        OnboardRESTClient client = parentCommand.getParentCommand().createClient();
        ListCapabilitiesResponse serviceProviderCapabilities = client.getServiceProviderCapabilities();
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(serviceProviderCapabilities));
        return 0;
    }
}
