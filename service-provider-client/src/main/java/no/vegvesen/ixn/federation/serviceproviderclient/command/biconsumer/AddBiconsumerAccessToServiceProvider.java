package no.vegvesen.ixn.federation.serviceproviderclient.command.biconsumer;

import no.vegvesen.ixn.federation.serviceproviderrestclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.BiqueueAccessResponse;
import picocli.CommandLine;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "on",
        description = "Add bi-consumer access to service provider",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """ 
                        Example:\n
                        serviceproviderclient bi-consumer on
                        """
        }
)
public class AddBiconsumerAccessToServiceProvider implements Callable<Integer> {

    @CommandLine.ParentCommand
    BiconsumerCommand parentCommand;

    @Override
    public Integer call() throws IOException {
        ServiceProviderClient client = parentCommand.getParent().createClient();

        ObjectMapper mapper = JsonMapper.builder().build();
        BiqueueAccessResponse response = new BiqueueAccessResponse(client.getUser(), true);
        BiqueueAccessResponse withAccess = client.addServiceProviderBiconsumerAccess(response);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(withAccess));
        return 0;
    }
}
