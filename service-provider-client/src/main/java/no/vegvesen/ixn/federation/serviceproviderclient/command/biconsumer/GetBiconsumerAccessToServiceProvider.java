package no.vegvesen.ixn.federation.serviceproviderclient.command.biconsumer;

import no.vegvesen.ixn.federation.serviceproviderrestclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.BiqueueAccessResponse;
import picocli.CommandLine;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

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
    public Integer call() throws JacksonException {
        ServiceProviderClient client = parentCommand.getParent().createClient();
        BiqueueAccessResponse biQueueAccess = client.getServiceProviderBiconsumerAccess();
        ObjectMapper mapper = JsonMapper.builder().build();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(biQueueAccess));
        return 0;
    }
}
