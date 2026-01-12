package no.vegvesen.ixn.federation.serviceproviderclient.command.biqueue;

import jakarta.jms.ExceptionListener;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.WriteToFileMessageListener;
import no.vegvesen.ixn.WriteToScreenMessageListener;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.*;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

@CommandLine.Command(name = "listen", description = "Listen to a bi-queue",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """ 
                        Examples:\n
                        serviceproviderclient bi-queue listen DENM -d directory \n
                        # -d is optional
                        """
        })
public class Listen implements Callable<Integer> {

    @CommandLine.ParentCommand
    BiqueueCommand parentCommand;

    @CommandLine.Option(names = {"-d", "--directory"}, description = "directory to save messages")
    String directory;

    @CommandLine.Parameters(index = "0", description = "Message type")
    String messageType;

    private final CountDownLatch counter = new CountDownLatch(1);

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().createClient();

        List<GetBiqueueEndpointsResponsePerMessageType> biqueueEndpointsResponse = client.listBiqueues();

        String biqueueName = biqueueEndpointsResponse.stream()
                .map(biqueueEndpointResponse -> biqueueEndpointResponse.getGetBiqueueEndpointResponse().getQueueName())
                .filter(s -> isMatched(s, messageType))
                .findFirst()
                .orElse(null);

        System.out.println(biqueueName);

        if (biqueueName == null) {
            throw new RuntimeException(String.format("Bi-queue %s does not exist!", biqueueName));
        }

        String brokerName = biqueueEndpointsResponse.stream()
                .filter(element -> element.getGetBiqueueEndpointResponse().getQueueName().equals(biqueueName))
                .map( o -> o.getGetBiqueueEndpointResponse().getBrokerExternalName())
                .findFirst().orElse(null);

        String url = "amqps://" + brokerName;

        System.out.printf("Listening for messages from queue [%s] on server [%s]%n", biqueueName, url);
        ExceptionListener exceptionListener = e -> {
            System.out.println("Exception received: " + e);
            counter.countDown();
        };
        try (Sink sink = new Sink(
                url,
                biqueueName,
                parentCommand.getParent().createSSLContext(),
                directory != null ? new WriteToFileMessageListener(directory) : new WriteToScreenMessageListener(),
                exceptionListener)
        ) {
            sink.start();
            counter.await();
        }
        return 0;
    }

    private static boolean isMatched(String id, String input) {
        String a = normalize(id);
        String b = normalize(input);
        return a.contains(b) || b.contains(a);
    }

    private static String normalize(String s) {
        return s.toLowerCase()
                .replace("_", "")
                .replace("-", "");
    }
}

