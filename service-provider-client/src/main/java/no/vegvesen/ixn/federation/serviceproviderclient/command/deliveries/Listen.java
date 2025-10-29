package no.vegvesen.ixn.federation.serviceproviderclient.command.deliveries;

import jakarta.jms.ExceptionListener;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.WriteToFileMessageListener;
import no.vegvesen.ixn.WriteToScreenMessageListener;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.*;
import picocli.CommandLine;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@CommandLine.Command(name = "listen", description = "Listen to a dlq for a delivery",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """ 
                        Examples:\n
                        serviceproviderclient deliveries listen -i 5a56dbcb-af41-4950-81f2-953e5cfcc4f9 -d directory \n
                        # -d is optional
                        """
        })
public class Listen implements Callable<Integer> {

    @CommandLine.ParentCommand
    DeliveriesCommand parentCommand;

    @CommandLine.Option(names = {"-d", "--directory"}, description = "directory to save messages")
    String directory;

    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
    DeliveriesOption option;

    private final CountDownLatch counter = new CountDownLatch(1);

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().createClient();

        String deliveryId;
        if (option.id != null) {
            deliveryId = option.id;
        } else {
            throw  new RuntimeException("Need to specify delivery id");
        }

        GetDeliveryResponse delivery = client.getDelivery(deliveryId);

        int maxRetries = 10;
        int retries = 0;

        while (delivery.getStatus().equals(DeliveryStatus.REQUESTED) && retries < maxRetries) {
            TimeUnit.SECONDS.sleep(3);
            delivery = client.getDelivery(deliveryId);
            retries++;
        }

        if (delivery.getStatus().equals(DeliveryStatus.REQUESTED)) {
            throw new RuntimeException(String.format("Delivery %s is still in REQUESTED state after %s retries", delivery.getId(), maxRetries));
        }

        if (!delivery.getStatus().equals(DeliveryStatus.CREATED)) {
            throw new RuntimeException(String.format("Unexpected delivery status: %s for delivery %s", delivery.getStatus(), delivery.getId()));
        }

        if (delivery.getEndpoints().stream().findFirst().orElseThrow().getDlqName() == null) {
            throw new RuntimeException(String.format("There is no dlq assigned for delivery %s", delivery.getId()));
        }

        DeliveryEndpoint deliveryEndpoint = delivery.getEndpoints().stream().findFirst().orElseThrow(() -> new RuntimeException("Could not determine delivery endpoint from response"));
        String url = "amqps://" + deliveryEndpoint.getHost();

        System.out.printf("Listening for messages from queue [%s] on server [%s]%n", deliveryEndpoint.getHost(), url);
        ExceptionListener exceptionListener = e -> {
            System.out.println("Exception received: " + e);
            counter.countDown();
        };
        try (Sink sink = new Sink(
                url,
                deliveryEndpoint.getTarget(),
                parentCommand.getParent().createSSLContext(),
                directory != null ? new WriteToFileMessageListener(directory) : new WriteToScreenMessageListener(),
                exceptionListener)
        ) {
            sink.start();
            counter.await();
        }
        return 0;
    }

    private static class DeliveriesOption {
        @CommandLine.Option(names = {"-i", "--id"}, required = true, description = "The delivery id")
        String id;
    }
}

