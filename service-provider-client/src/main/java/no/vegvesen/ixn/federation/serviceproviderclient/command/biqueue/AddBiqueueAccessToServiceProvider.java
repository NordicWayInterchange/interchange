package no.vegvesen.ixn.federation.serviceproviderclient.command.biqueue;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.AddBiqueueAccessRequest;
import no.vegvesen.ixn.serviceprovider.model.BiqueueAccessResponse;
import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "on",
        description = "Add or revoke bi-queue access to service provider",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """ 
                        Example:\n
                        serviceproviderclient bi-queue on
                        """
        }
)
public class AddBiqueueAccessToServiceProvider implements Callable<Integer> {

    @CommandLine.ParentCommand
    BiqueueCommand parentCommand;

    @Override
    public Integer call() throws IOException {
        ServiceProviderClient client = parentCommand.getParent().createClient();

        ObjectMapper mapper = new ObjectMapper();
        AddBiqueueAccessRequest request = new AddBiqueueAccessRequest(true);
        request.setAccess(true);
        BiqueueAccessResponse withAccess = client.addServiceProviderBiconsumerAccess(request);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(withAccess));
        return 0;
    }
}
