package no.vegvesen.ixn.federation.serviceproviderclient.command.biconsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderrestclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.BiqueueAccessResponse;
import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "off",
        description = "Remove bi-consumer access to service provider",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """ 
                        Example:\n
                        serviceproviderclient bi-consumer off
                        """
        }
)
public class RemoveBiconsumerAccessToServiceProvider implements Callable<Integer> {

    @CommandLine.ParentCommand
    BiconsumerCommand parentCommand;

    @Override
    public Integer call() throws IOException {
        ServiceProviderClient client = parentCommand.getParent().createClient();

        ObjectMapper mapper = new ObjectMapper();
        BiqueueAccessResponse response = new BiqueueAccessResponse(client.getUser(), false);
        BiqueueAccessResponse withAccess = client.addServiceProviderBiconsumerAccess(response);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(withAccess));
        return 0;
    }
}
