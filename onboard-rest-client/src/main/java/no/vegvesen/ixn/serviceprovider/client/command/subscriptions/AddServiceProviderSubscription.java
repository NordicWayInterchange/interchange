package no.vegvesen.ixn.serviceprovider.client.command.subscriptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.serviceprovider.client.OnboardRESTClient;
import no.vegvesen.ixn.serviceprovider.model.AddSubscription;
import no.vegvesen.ixn.serviceprovider.model.AddSubscriptionsRequest;
import no.vegvesen.ixn.serviceprovider.model.AddSubscriptionsResponse;
import static picocli.CommandLine.*;

import java.io.File;
import java.util.Set;
import java.util.concurrent.Callable;

@Command(
        name = "add",
        description = "Add a subscription for the service provider",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class AddServiceProviderSubscription implements Callable<Integer> {

    @ParentCommand
    SubscriptionsCommand parentCommand;

    @ArgGroup(exclusive = true, multiplicity = "1")
    AddServiceProviderSubscriptionOption option;

    @Override
    public Integer call() throws Exception {
        OnboardRESTClient client = parentCommand.getParent().createClient();
        ObjectMapper mapper = new ObjectMapper();

        if(option.file != null) {
            AddSubscriptionsRequest requestApi = mapper.readValue(option.file, AddSubscriptionsRequest.class);
            AddSubscriptionsResponse result = client.addSubscription(requestApi);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        }
        else{
            AddSubscriptionsRequest requestApi = new AddSubscriptionsRequest(client.getUser(), Set.of(new AddSubscription(option.selector)));
            AddSubscriptionsResponse result = client.addSubscription(requestApi);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        }
        return 0;
    }
}

class AddServiceProviderSubscriptionOption {
    @Option(names = {"-f", "--filename"}, required = true, description = "The subscription json file")
    File file;

    @Option(names = {"-s", "--selector"}, required = true, description = "The subscription selector")
    String selector;
}
