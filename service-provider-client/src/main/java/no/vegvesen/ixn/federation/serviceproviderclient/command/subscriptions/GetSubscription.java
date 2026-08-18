package no.vegvesen.ixn.federation.serviceproviderclient.command.subscriptions;

import no.vegvesen.ixn.federation.serviceproviderrestclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.GetSubscriptionResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.PropertiesDefaultProvider;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.Callable;

@Command(
        name = "get",
        description = "Get details on a specific subscription for a Service Provider",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """ 
                        Example:\n
                        serviceproviderclient subscriptions get 75c47bbb-af97-4751-9fca-6328325c9a2d
                        """
        }
)
public class GetSubscription implements Callable<Integer> {

    @ParentCommand
    SubscriptionsCommand parentCommand;

    @Parameters(index = "0", description = "The ID of the subscription with the brokerUrl")
    String subscriptionId;

    @Override
    public Integer call() throws JacksonException {
        ServiceProviderClient client = parentCommand.getParent().createClient();
        GetSubscriptionResponse subscription = client.getSubscription(subscriptionId);
        System.out.printf("Subscription %s successfully polled with %n", subscriptionId);
        ObjectMapper mapper = JsonMapper.builder().build();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(subscription));
        return 0;
    }
}