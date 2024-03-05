package no.vegvesen.ixn.serviceprovider.client.command.subscriptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.serviceprovider.client.OnboardRESTClient;
import no.vegvesen.ixn.serviceprovider.client.OnboardRestClientApplication;
import no.vegvesen.ixn.serviceprovider.model.GetSubscriptionResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(
        name = "get",
        description = "Get details on a specific subscription for a Service Provider",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class GetSubscription implements Callable<Integer> {

    @ParentCommand
    SubscriptionsCommand parentCommand;

    @Parameters(index = "0", description = "The ID of the subscription with the brokerUrl")
    Integer subscriptionId;

    @Override
    public Integer call() throws JsonProcessingException {
        OnboardRESTClient client = parentCommand.getParent().createClient();
        GetSubscriptionResponse subscription = client.getSubscription(subscriptionId);
        System.out.printf("Subscription %d successfully polled with %n", subscriptionId);
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(subscription));
        return 0;
    }
}
