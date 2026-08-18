package no.vegvesen.ixn.federation.serviceproviderclient.command.biqueue;

import no.vegvesen.ixn.federation.serviceproviderrestclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.GetBiqueueEndpointsResponsePerMessageType;
import picocli.CommandLine;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.concurrent.Callable;


@CommandLine.Command(
        name = "list",
        description = "List biqueues",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """
                        Example: \n
                        serviceproviderclient bi-queue list
                        """
        }
)
public class ListBiqueues implements Callable<Integer> {

    @CommandLine.ParentCommand
    BiqueueCommand parentCommand;

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().createClient();
        ObjectMapper mapper = JsonMapper.builder().build();
        List<GetBiqueueEndpointsResponsePerMessageType> response = client.listBiqueues();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        return 0;
    }
}
