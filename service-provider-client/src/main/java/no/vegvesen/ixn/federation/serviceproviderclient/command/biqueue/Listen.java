package no.vegvesen.ixn.federation.serviceproviderclient.command.biqueue;

import jakarta.jms.ExceptionListener;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.WriteToFileMessageListener;
import no.vegvesen.ixn.WriteToScreenMessageListener;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.*;
import picocli.CommandLine;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

@CommandLine.Command(name = "listen", description = "Listen to a bi-queue",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """ 
                        Examples:\n
                        serviceproviderclient bi-queue listen -d directory \n
                        # -d is optional
                        """
        })
public class Listen implements Callable<Integer> {

    @CommandLine.ParentCommand
    BiqueueCommand parentCommand;

    @CommandLine.Option(names = {"-d", "--directory"}, description = "directory to save messages")
    String directory;

    private final CountDownLatch counter = new CountDownLatch(1);

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().createClient();

        GetBiqueueEndpointsResponse biqueueEndpointResponse = client.getBiqueueEndpoint();

        String biqueueName = biqueueEndpointResponse.getQueueName();

        String url = "amqps://" + biqueueEndpointResponse.getBrokerExternalName();

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
}