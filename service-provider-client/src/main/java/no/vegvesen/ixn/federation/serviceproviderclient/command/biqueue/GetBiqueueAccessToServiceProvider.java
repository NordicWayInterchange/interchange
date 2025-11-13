package no.vegvesen.ixn.federation.serviceproviderclient.command.biqueue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.BiQueueAccessResponse;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "show",
        description = "show if the service provider has access to bi queue",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """ 
                        Example:\n
                        serviceproviderclient bi-queue show
                        """
        }
)
public class GetBiqueueAccessToServiceProvider implements Callable<Integer> {
    @CommandLine.ParentCommand
    BiqueueCommand parentCommand;

    @Override
    public Integer call() throws JsonProcessingException {
        ServiceProviderClient client = parentCommand.getParent().createClient();
        BiQueueAccessResponse biQueueAccess = client.getServiceProviderBiconsumerAccess();
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(biQueueAccess));
        return 0;
    }
}
