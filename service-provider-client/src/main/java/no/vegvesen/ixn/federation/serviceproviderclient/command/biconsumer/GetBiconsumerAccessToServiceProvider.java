package no.vegvesen.ixn.federation.serviceproviderclient.command.biconsumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderrestclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.BiqueueAccessResponse;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "show",
        description = "show if the service provider has access to bi-consumer",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """ 
                        Example:\n
                        serviceproviderclient bi-consumer show
                        """
        }
)
public class GetBiconsumerAccessToServiceProvider implements Callable<Integer> {
    @CommandLine.ParentCommand
    BiconsumerCommand parentCommand;

    @Override
    public Integer call() throws JsonProcessingException {
        ServiceProviderClient client = parentCommand.getParent().createClient();
        BiqueueAccessResponse biQueueAccess = client.getServiceProviderBiconsumerAccess();
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(biQueueAccess));
        return 0;
    }
}
