package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.peers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.ExceptionListener;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.WriteToFileMessageListener;
import no.vegvesen.ixn.WriteToScreenMessageListener;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.*;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@CommandLine.Command(name = "listen", description = "Add private channel peers and receive messages",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """ 
                        Examples: \n
                        serviceproviderclient privatechannels peers listen -i 5a56dbcb-af41-4950-81f2-953e5cfcc4f9 \n
                        """
        })

public class Listen implements Callable<Integer> {

    @CommandLine.ParentCommand
    PeersCommand parentCommand;

    @CommandLine.ArgGroup(multiplicity = "1")
    PrivateChannelsOption option;

    @CommandLine.Option(names = {"-d", "--directory"}, description = "directory to save messages")
    String directory;

    private final CountDownLatch counter = new CountDownLatch(1);

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().getParent().createClient();

        String id = "";
       /* Do we need to read peers from a file?
       if (option.file != null) {
            ObjectMapper mapper = new ObjectMapper();
            AddPeersRequest request = mapper.readValue(option.file, AddPeersRequest.class);
            client.addPeersToPrivateChannel(option.id, request);
            System.out.printf("successfully added %s to private channel with id %s", request.getPeersToAdd(), option.id);

        } else */
        if (option.id != null) {
            id = option.id;

        } else {
            throw new RuntimeException("Need to specify either id or file");
        }

        GetPrivateChannelResponse privateChannel = client.getPrivateChannel(id);
        while (privateChannel.getStatus().equals(PrivateChannelStatusApi.REQUESTED)) {
            privateChannel = client.getPrivateChannel(privateChannel.getId());
            TimeUnit.SECONDS.sleep(2);
        }

        if (!privateChannel.getStatus().equals(PrivateChannelStatusApi.CREATED)) {
            throw new RuntimeException(String.format("Unexpected private channel status %s for private channel %s", privateChannel.getStatus(), privateChannel.getId()));
        }

        PrivateChannelEndpointApi endpointApi = client
                .getPeerPrivateChannels()
                .getPrivateChannels()
                .getFirst()
                .getEndpoint();

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

    private static class PrivateChannelsOption {
        @CommandLine.Option(names = {"-f", "--filename"}, description = "The private channel json file")
        File file;

        @CommandLine.Option(names = {"-i", "--id"}, description = "The private channel Id")
        String id;
    }
}
