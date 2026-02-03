package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.apache.qpid.jms.message.JmsMessage;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


@CommandLine.Command(name = "send",
        description = "Add private channel and send message",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """
                        Examples: \n
                        serviceproviderclient privatechannels send -m message.json -i 5d16cb60-0534-4469-b525-f92a5953322c \n
                        serviceproviderclient privatechannels send -m message.json -f privatechannels.json -b \n
                        """
        })
public class Send implements Callable<Integer> {

    @CommandLine.ParentCommand
    PrivateChannelsCommand parentCommand;

    @CommandLine.Option(names = {"-m", "--message"}, description = "The message json file", required = true)
    File messageFile;

    @CommandLine.Option(names = {"-b", "--binary"}, description = "Send file")
    boolean binary;

    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
    PrivatechannelsOption option;

    @Override
    public Integer call() throws Exception {

        ServiceProviderClient client = parentCommand.getParent().createClient();
        String privateChannelId;
        if (option.file != null) {
            ObjectMapper mapper = new ObjectMapper();
            AddPrivateChannelRequest privateChannelRequest = mapper.readValue(option.file, AddPrivateChannelRequest.class);
            AddPrivateChannelResponse privateChannelResponse = client.addPrivateChannel(privateChannelRequest);
            privateChannelId = privateChannelResponse.getPrivateChannels().stream().findFirst().orElseThrow(() -> new RuntimeException("Server indicated private channel was created, " +
                    "but could not find it in response")).getId();
        } else {
            privateChannelId = option.id;
        }

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

        System.out.printf("Sending message from file %s%n", messageFile);

        try (Source source = new Source(url, queueName, parentCommand.getParent().createSSLContext())) {
            JmsMessage message1 = source.createMessageBuilder()
                    .textMessage("fishy fishy")
                    .userId(privateChannelEndpoint.getHost())
                    .build();
            source.send(message1);
        }
        return 0;
    }

    private static class PrivatechannelsOption {
        @CommandLine.Option(names = {"-f", "--file"}, required = true, description = "The privatechannel json file")
        File file;

        @CommandLine.Option(names = {"-i", "--id"}, required = true, description = "The privatechannel id")
        String id;
    }
}


