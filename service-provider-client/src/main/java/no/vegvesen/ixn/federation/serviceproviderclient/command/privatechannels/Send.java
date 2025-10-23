package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.InvalidDestinationException;
import no.vegvesen.ixn.MessageBuilder;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.federation.serviceproviderclient.messages.*;
import no.vegvesen.ixn.serviceprovider.model.*;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static no.vegvesen.ixn.federation.api.v1_0.Constants.*;


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

        int attempts = 0;
        int maxAttempts = 10;
        while (privateChannel.getStatus().equals(PrivateChannelStatusApi.REQUESTED) && attempts < maxAttempts) {
            TimeUnit.SECONDS.sleep(3);
            privateChannel = client.getPrivateChannel(privateChannelId);
            attempts++;
        }
        if (attempts == maxAttempts) {
            throw new RuntimeException("Timeout waiting for private channel status to change from REQUESTED to CREATED");
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
        ObjectMapper mapper = new ObjectMapper();
        Messages messages = mapper.readValue(messageFile, Messages.class);
        validateInput(messages);
        try (Source source = new Source(url, queueName, parentCommand.getParent().createSSLContext())) {

            while (true) {
                try {
                    source.start();
                    break;
                } catch (InvalidDestinationException e) {
                    System.out.println("\nRetrying\n");
                    TimeUnit.SECONDS.sleep(3);
                }
            }

            for (Message message : messages.getMessages()) {
                MessageBuilder messageBuilder = source.createMessageBuilder();
                switch (message) {
                    case DenmMessage ignored -> {
                        messageBuilder
                                .bytesMessage(binary ? convertFileToByteArray(message.getFileName()) : message.getMessageText().getBytes(StandardCharsets.UTF_8))
                                .messageType(DENM)
                                .causeCode(((DenmMessage) message).getCauseCode())
                                .subCauseCode(((DenmMessage) message).getSubCauseCode());
                    }
                    case DatexMessage ignored -> {
                        messageBuilder
                                .textMessage(message.getMessageText())
                                .messageType(DATEX_2)
                                .publicationType(((DatexMessage) message).getPublicationType())
                                .publicationSubType(((DatexMessage) message).getPublicationSubType())
                                .publisherName(((DatexMessage) message).getPublisherName());
                    }
                    case IvimMessage ignored -> {
                        messageBuilder
                                .bytesMessage(binary ? convertFileToByteArray(message.getFileName()) : message.getMessageText().getBytes(StandardCharsets.UTF_8))
                                .messageType(IVIM)
                                .iviType(((IvimMessage) message).getIviType())
                                .pictogramCategoryCode(((IvimMessage) message).getPictogramCategoryCode())
                                .iviContainer(((IvimMessage) message).getIviContainer());
                    }
                    case SpatemMessage ignored -> {
                        messageBuilder
                                .bytesMessage(binary ? convertFileToByteArray(message.getFileName()) : message.getMessageText().getBytes(StandardCharsets.UTF_8))
                                .messageType(SPATEM)
                                .id(((SpatemMessage) message).getId())
                                .name(((SpatemMessage) message).getName());
                    }
                    case MapemMessage ignored -> {
                        messageBuilder
                                .bytesMessage(binary ? convertFileToByteArray(message.getFileName()) : message.getMessageText().getBytes(StandardCharsets.UTF_8))
                                .messageType(MAPEM)
                                .id(((MapemMessage) message).getId())
                                .name(((MapemMessage) message).getName());
                    }
                    case SsemMessage ignored -> {
                        messageBuilder
                                .bytesMessage(binary ? convertFileToByteArray(message.getFileName()) : message.getMessageText().getBytes(StandardCharsets.UTF_8))
                                .messageType(SSEM)
                                .id(((SsemMessage) message).getId());
                    }
                    case SremMessage ignored -> {
                        messageBuilder
                                .bytesMessage(binary ? convertFileToByteArray(message.getFileName()) : message.getMessageText().getBytes(StandardCharsets.UTF_8))
                                .messageType(SREM)
                                .id(((SremMessage) message).getId());
                    }
                    case CamMessage ignored -> {
                        messageBuilder
                                .bytesMessage(binary ? convertFileToByteArray(message.getFileName()) : message.getMessageText().getBytes(StandardCharsets.UTF_8))
                                .messageType(CAM)
                                .stationType(((CamMessage) message).getStationType())
                                .vehicleRole(((CamMessage) message).getVehicleRole());
                    }

                    default -> throw new Exception("Message is not of valid messagetype");
                }

                messageBuilder
                        .userId(message.getUserId())
                        .publisherId(message.getPublisherId())
                        .publicationId(message.getPublicationId())
                        .originatingCountry(message.getOriginatingCountry())
                        .protocolVersion(message.getProtocolVersion())
                        .longitude(message.getLongitude())
                        .latitude(message.getLatitude())
                        .quadTreeTiles(message.getQuadTree())
                        .shardId(message.getShardId())
                        .shardCount(message.getShardCount())
                        .baselineVersion(message.getBaselineVersion())
                        .serviceType(message.getServiceType());
                source.send(messageBuilder.build());

            }
        }
        return 0;
    }

    private static byte[] convertFileToByteArray(String fileName) throws IOException {
        File file = new File(fileName);
        return Files.readAllBytes(file.toPath());
    }

    private void validateInput(Messages messages) throws Exception {
        for (Message message : messages.getMessages()) {
            if (binary) {
                if (message.getMessageType().equals(DATEX_2)) {
                    throw new Exception("DATEX messages can not be sent binary.");
                }
                if (message.getFile() == null) {
                    throw new Exception("Message does not contain file");
                }
            } else {
                if (message.getMessageText() == null) {
                    throw new Exception("Message does not contain messageText");
                }
            }
        }
    }

    private static class PrivatechannelsOption {
        @CommandLine.Option(names = {"-f", "--file"}, required = true, description = "The privatechannel json file")
        File file;

        @CommandLine.Option(names = {"-i", "--id"}, required = true, description = "The privatechannel id")
        String id;
    }
}


