package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels;

import jakarta.jms.ExceptionListener;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.WriteToFileMessageListener;
import no.vegvesen.ixn.WriteToScreenMessageListener;
import no.vegvesen.ixn.federation.serviceproviderrestclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.*;
import picocli.CommandLine;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@CommandLine.Command(name = "listen", description = "Listen to messages on a private channel",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """ 
                        Examples: \n
                        serviceproviderclient privatechannels listen -d directory 5a56dbcb-af41-4950-81f2-953e5cfcc4f9 \n
                        # -d is optional
                        """
        })

public class Listen implements Callable<Integer> {

    @CommandLine.ParentCommand
    PrivateChannelsCommand parentCommand;

    @CommandLine.Option(names = {"-d", "--directory"}, description = "directory to save messages")
    String directory;

    @CommandLine.Parameters(index = "0", description = "The ID of the private channel to listen to")
    String id;

    private final CountDownLatch counter = new CountDownLatch(1);

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().createClient();

        GetPrivateChannelResponse privateChannel = client.getPrivateChannel(id);

        int maxRetries = 10;
        int retries = 0;

        while (privateChannel.getStatus().equals(PrivateChannelStatusApi.REQUESTED) && retries < maxRetries) {
            TimeUnit.SECONDS.sleep(3);
            privateChannel = client.getPrivateChannel(privateChannel.getId());
            retries++;
        }

        if (!privateChannel.getStatus().equals(PrivateChannelStatusApi.CREATED)) {
            throw new RuntimeException(String.format("Unexpected private channel status %s for private channel %s", privateChannel.getStatus(), privateChannel.getId()));
        }

        PrivateChannelEndpointApi endpointApi = privateChannel.getEndpoint();
        if (endpointApi == null) {
            throw new RuntimeException("Could not determine private channel endpoint from response ");
        }
        String url = "amqps://" + endpointApi.getHost();

        System.out.printf("Listening for messages from queue [%s] on server [%s]%n", endpointApi.getQueueName(), url);
        ExceptionListener exceptionListener = e -> {
            System.out.println("Exception received: " + e);
            counter.countDown();
        };
        try (Sink sink = new Sink(
                url,
                endpointApi.getQueueName(),
                parentCommand.getParent().createSSLContext(),
                directory != null ? new WriteToFileMessageListener(directory) : new WriteToScreenMessageListener(),
                exceptionListener)
        ) {
            sink.start();
            counter.await();
        }
        return 0;
    }

}
