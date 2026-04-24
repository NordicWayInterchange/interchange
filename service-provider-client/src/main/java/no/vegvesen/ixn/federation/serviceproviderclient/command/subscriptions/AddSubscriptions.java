package no.vegvesen.ixn.federation.serviceproviderclient.command.subscriptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderrestclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.AddSubscription;
import no.vegvesen.ixn.serviceprovider.model.AddSubscriptionsRequest;
import no.vegvesen.ixn.serviceprovider.model.AddSubscriptionsResponse;
import picocli.CommandLine.*;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "add",
        description = "Add a subscription for the service provider",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """
                        Examples:\n
                        serviceproviderclient subscriptions add -s "originatingCountry='NO'" -d "Description to subscription"\n
                        serviceproviderclient subscriptions add -f denm_sub.json \n
                        -d is optional
                        """
        }
)
public class AddSubscriptions implements Callable<Integer> {

    @ParentCommand
    SubscriptionsCommand parentCommand;

    @ArgGroup(exclusive = true, multiplicity = "1")
    AddServiceProviderSubscriptionOption option;

    @Option(names = {"-d", "--description"})
    String description;

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().createClient();
        ObjectMapper mapper = new ObjectMapper();

        if(option.file != null) {
            AddSubscriptionsRequest requestApi = mapper.readValue(option.file, AddSubscriptionsRequest.class);
            AddSubscriptionsResponse result = client.addSubscription(requestApi);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        }
        else{
            AddSubscriptionsRequest requestApi = new AddSubscriptionsRequest(client.getUser(), List.of(new AddSubscription(option.selector, description)));
            AddSubscriptionsResponse result = client.addSubscription(requestApi);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        }
        return 0;
    }

    static class AddServiceProviderSubscriptionOption {
        @Option(names = {"-f", "--filename"}, required = true, description = "The subscription json file")
        File file;

        @Option(names = {"-s", "--selector"}, required = true, description = "The subscription selector")
        String selector;
    }
}
