package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.peers;


import jakarta.jms.ExceptionListener;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.WriteToFileMessageListener;
import no.vegvesen.ixn.WriteToScreenMessageListener;
import no.vegvesen.ixn.federation.serviceproviderrestclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.PeerPrivateChannelApi;
import no.vegvesen.ixn.serviceprovider.model.PrivateChannelEndpointApi;
import no.vegvesen.ixn.serviceprovider.model.PrivateChannelStatusApi;
import picocli.CommandLine;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@CommandLine.Command(name = "listen", description = "Listen to messages on a peer",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """ 
                        Examples: \n
                        serviceproviderclient privatechannels peers listen -d directory 5a56dbcb-af41-4950-81f2-953e5cfcc4f9 \n
                        # -d is optional
                        """
        })

public class Listen implements Callable<Integer> {

    @CommandLine.ParentCommand
    PeersCommand parentCommand;

    @CommandLine.Option(names = {"-d", "--directory"}, description = "directory to save messages")
    String directory;

    @CommandLine.Parameters(index = "0", description = "The ID of private channel to listen to")
    String id;

    private final CountDownLatch counter = new CountDownLatch(1);

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().getParent().createClient();

        PeerPrivateChannelApi privateChannelPeer = client.getPrivateChannelPeerById(id);

        int maxRetries = 10;
        int retries = 0;

        while (privateChannelPeer.getStatus().equals(PrivateChannelStatusApi.REQUESTED) && retries < maxRetries) {
            TimeUnit.SECONDS.sleep(3);
            privateChannelPeer = client.getPrivateChannelPeerById(privateChannelPeer.getId());
            retries++;
        }

        if (!privateChannelPeer.getStatus().equals(PrivateChannelStatusApi.CREATED)) {
            throw new RuntimeException(String.format("Unexpected private channel status %s for private channel %s", privateChannelPeer.getStatus(), privateChannelPeer.getId()));
        }

        PrivateChannelEndpointApi endpointApi = privateChannelPeer.getEndpoint();
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
                parentCommand.getParent().getParent().createSSLContext(),
                directory != null ? new WriteToFileMessageListener(directory) : new WriteToScreenMessageListener(),
                exceptionListener)
        ) {
            sink.start();
            counter.await();
        }
        return 0;
    }

}
