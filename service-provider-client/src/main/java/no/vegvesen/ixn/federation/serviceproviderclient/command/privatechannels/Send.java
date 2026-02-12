package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.InvalidDestinationException;
import no.vegvesen.ixn.MessageBuilder;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.messages.PrivateTextMessage;
import no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.messages.PrivateTextMessages;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.apache.qpid.jms.message.JmsMessage;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
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
                        serviceproviderclient privatechannels send -m myMessages.json 5d16cb60-0534-4469-b525-f92a5953322c \n
                        """
        })
public class Send implements Callable<Integer> {

    @CommandLine.ParentCommand
    PrivateChannelsCommand parentCommand;

    @CommandLine.Option(names = {"-m", "--message"}, description = "The message body", required = true)
    String messageFileName;

    @CommandLine.Parameters(index = "0")
    String privateChannelId;


    @Override
    public Integer call() throws Exception {

        ServiceProviderClient client = parentCommand.getParent().createClient();

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

        ObjectMapper mapper = new ObjectMapper();

        try (Source source = new Source(url, queueName, parentCommand.getParent().createSSLContext())) {
            source.start();

            PrivateTextMessages privateTextMessages = mapper.readValue(Path.of(messageFileName).toFile(), PrivateTextMessages.class);

            for (PrivateTextMessage message : privateTextMessages.privateTextMessages()) {
                JmsMessage textMessage = source.createTextMessage(message.messageText());
                Set<String> properties = message.messageProperties().keySet();
                for (String property : properties) {
                   textMessage.setObjectProperty(property, message.messageProperties().get(property));
                }
                source.send(textMessage);
            }
        }
        return 0;
    }

}


