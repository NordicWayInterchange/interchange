package no.vegvesen.ixn.napcore.client.command.subscriptions;

import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.Subscription;
import no.vegvesen.ixn.napcore.model.SubscriptionRequest;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(
        name = "add",
        description = "Add NAP subscription from file",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class AddNapSubscription implements Callable<Integer> {

    @ParentCommand
    SubscriptionsCommand parentCommand;

    @ArgGroup(exclusive = true, multiplicity = "1")
    AddNapSubscriptionOption option;

    @Option(names = {"-d", "--description"})
    String description;

    @Override
    public Integer call() throws IOException {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        ObjectMapper mapper = JsonMapper.builder().build();

        if (option.file != null) {
            SubscriptionRequest request = mapper.readValue(option.file, SubscriptionRequest.class);
            Subscription result = client.addSubscription(request);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        }
        else {
            SubscriptionRequest request = new SubscriptionRequest(option.selector, description);
            Subscription result = client.addSubscription(request);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        }
        return 0;
    }

    static class AddNapSubscriptionOption {
        @Option(names = {"-f", "--filename"}, required = true, description = "The subscription json file")
        File file;

        @Option(names = {"-s", "--selector"}, required = true, description = "The subscription selector")
        String selector;
    }
}

