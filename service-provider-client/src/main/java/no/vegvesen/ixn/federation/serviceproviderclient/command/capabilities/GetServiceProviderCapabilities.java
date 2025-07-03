package no.vegvesen.ixn.federation.serviceproviderclient.command.capabilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.ListCapabilitiesResponse;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Command(
        name = "list",
        description = "List the service provider capabilities",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
        """ 
                Example:\n
                serviceproviderclient capabilities get a4154ea0-77aa-4f04-9e3f-c325430fd2be
                """
}
)
public class GetServiceProviderCapabilities implements Callable<Integer> {

    @ParentCommand
    CapabilitiesCommand parentCommand;

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().createClient();
        ListCapabilitiesResponse serviceProviderCapabilities = client.getServiceProviderCapabilities();
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(serviceProviderCapabilities));
        return 0;
    }
}
