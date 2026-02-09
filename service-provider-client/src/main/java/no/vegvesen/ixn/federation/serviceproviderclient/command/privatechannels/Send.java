package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels;

import jakarta.jms.InvalidDestinationException;
import no.vegvesen.ixn.MessageBuilder;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.*;
import picocli.CommandLine;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


@CommandLine.Command(name = "send",
        description = "Send message to a private channel",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """
                        Examples: \n
                        serviceproviderclient privatechannels send -m "Hello world!" -v "propertyValue" -i 5d16cb60-0534-4469-b525-f92a5953322c \n
                        """
        })
public class Send implements Callable<Integer> {

    @CommandLine.ParentCommand
    PrivateChannelsCommand parentCommand;

    @CommandLine.Option(names = {"-m", "--message"}, description = "The message body", required = true)
    String messageBody;

    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
    PrivatechannelsOption option;

    @CommandLine.Option(names = {"-v", "--value"}, description = "Value of the object property (String or boxed type)", required = true)
    String propertyValue;

    @Override
    public Integer call() throws Exception {

        ServiceProviderClient client = parentCommand.getParent().createClient();
        String privateChannelId = option.id;

        GetPrivateChannelResponse privateChannel = client.getPrivateChannel(privateChannelId);

        int maxRetries = 10;
        int retries = 0;
        while (privateChannel.getStatus().equals(PrivateChannelStatusApi.REQUESTED) && retries < maxRetries) {
            TimeUnit.SECONDS.sleep(3);
            privateChannel = client.getPrivateChannel(privateChannelId);
            retries++;
        }

        if (privateChannel.getStatus().equals(PrivateChannelStatusApi.REQUESTED)) {
            throw new RuntimeException(String.format("Private channel %s is still in REQUESTED state after the timeout", privateChannel.getId()));
        }
        if (!privateChannel.getStatus().equals(PrivateChannelStatusApi.CREATED)) {
            throw new RuntimeException(String.format("Unexpected private channel status: %s for privatechannel %s", privateChannel.getStatus(), privateChannel.getId()));
        }

        PrivateChannelEndpointApi privateChannelEndpoint = privateChannel.getEndpoint();
        if (privateChannelEndpoint == null) {
            throw new RuntimeException("Could not determine private channel endpoint from response ");
        }
        String queueName = privateChannelEndpoint.getQueueName();
        String url = "amqps://" + privateChannelEndpoint.getHost();

        System.out.printf("Sending message %s and propertyValue %s%n", messageBody, propertyValue);

        try (Source source = new Source(url, queueName, parentCommand.getParent().createSSLContext())) {
            source.start();

            MessageBuilder messageBuilder = source.createMessageBuilder()
                    .textMessage(messageBody)
                    .objectProperty("objectValue", propertyValue);

            source.send(messageBuilder.build());
        }
        return 0;
    }

    private static class PrivatechannelsOption {

        @CommandLine.Option(names = {"-i", "--id"}, required = true, description = "The privatechannel id")
        String id;
    }
}


