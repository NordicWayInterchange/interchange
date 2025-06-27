package no.vegvesen.ixn.federation.serviceproviderclient.command.subscriptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.ExceptionListener;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.*;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Command(name = "listen", description = "Add subscription and receive messages",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """ 
                        Examples:\n
                        serviceproviderclient subscriptions listen -s "originatingCountry='NO'" \n
                        serviceproviderclient subscriptions listen -i 5a56dbcb-af41-4950-81f2-953e5cfcc4f9 \n
                        serviceproviderclient subscriptions listen -f sub.json \n
                        serviceproviderclient subscriptions listen -s "originatingCountry='NO'" -d directory \n
                        serviceproviderclient subscriptions listen -s "originatingCountry='NO'" -d directory -c "NO subscription" \n
                        # -d and -c is optional
                        """
        })
public class Listen implements Callable<Integer> {

    @ParentCommand
    SubscriptionsCommand parentCommand;

    @ArgGroup(multiplicity = "1")
    SubscriptionsOption option;

    @Option(names = {"-d", "--directory"}, description = "directory to save messages")
    String directory;

    @Option(names = {"-c", "--comment"})
    String description;

    private final CountDownLatch counter = new CountDownLatch(1);

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().createClient();

        String id;
        if(option.file != null){
            ObjectMapper mapper = new ObjectMapper();
            AddSubscriptionsRequest request = mapper.readValue(option.file, AddSubscriptionsRequest.class);
            AddSubscriptionsResponse addSubscriptionsResponse = client.addSubscription(request);
            id = addSubscriptionsResponse.getSubscriptions().stream()
                    .filter(sub -> sub.getSelector().equals(addSubscriptionsResponse.getSubscriptions().stream().findFirst().orElseThrow(() -> new RuntimeException("Could not find subscription with requested selector")).getSelector()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Server indicated subscription was added, but could not find it in response"))
                    .getId();
        }
        else if(option.selector != null){
            AddSubscriptionsResponse addSubscriptionsResponse = client.addSubscription(new AddSubscriptionsRequest(client.getUser(), List.of(new AddSubscription(option.selector, description))));
            id = addSubscriptionsResponse.getSubscriptions().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Server indicated subscription was added, but could not find it in response"))
                    .getId();
        } else if (option.id != null) {
            id = option.id;

        } else {
            throw  new RuntimeException("Need to specify either id, selector or file");
        }

        GetSubscriptionResponse subscription = client.getSubscription(id);
        while (subscription.getStatus().equals(LocalActorSubscriptionStatusApi.REQUESTED)) {
            subscription = client.getSubscription(subscription.getId());
            TimeUnit.SECONDS.sleep(2);
        }

        if (! subscription.getStatus().equals(LocalActorSubscriptionStatusApi.CREATED)) {
            throw new RuntimeException(String.format("Unexpected subscription status %s for subscription %s",subscription.getStatus(),subscription.getId()));

        }

        LocalEndpointApi endpointApi = client
                .getSubscription(subscription.getId())
                .getEndpoints()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Could not determine endpoint for subscription with id %s",id)));
        String url = "amqps://"+endpointApi.getHost();

        System.out.printf("Listening for messages from queue [%s] on server [%s]%n", endpointApi.getHost(), url);
        ExceptionListener exceptionListener = e -> {
            System.out.println("Exception received: " + e);
            counter.countDown();
        };
        try (Sink sink = new Sink(
                url,
                endpointApi.getSource(),
                parentCommand.getParent().createSSLContext(),
                directory != null ? new Sink.DefaultMessageListener(directory) : new Sink.DefaultMessageListener(),
                exceptionListener)
        ) {
            sink.start();
            counter.await();
        }
        return 0;
    }

    private static class SubscriptionsOption {
        @Option(names = {"-f", "--filename"}, description = "The subscription json file")
        File file;

        @Option(names = {"-s", "--selector"}, description = "The subscriptions selector")
        String selector;

        @Option(names = {"-i", "--id"}, description = "The subscription Id")
        String id;
    }
}

