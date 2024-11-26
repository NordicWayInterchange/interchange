package no.vegvesen.ixn.federation.serviceproviderclient.command.subscriptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.ListSubscriptionsResponse;
import picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Command(
        name = "list",
        description = "List the service provider subscriptions",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class GetSubscriptions implements Callable<Integer> {

    @ParentCommand
    SubscriptionsCommand parentCommand;

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().createClient();
        ListSubscriptionsResponse subscriptions = client.getSubscriptions();
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(subscriptions));
        return 0;
    }
}