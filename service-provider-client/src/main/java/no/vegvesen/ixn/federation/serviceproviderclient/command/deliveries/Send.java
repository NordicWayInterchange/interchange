package no.vegvesen.ixn.federation.serviceproviderclient.command.deliveries;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.MessageBuilder;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.federation.serviceproviderclient.command.subscriptions.SubscriptionsCommand;
import no.vegvesen.ixn.federation.serviceproviderclient.messages.*;
import static picocli.CommandLine.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import static no.vegvesen.ixn.federation.api.v1_0.Constants.*;
import static no.vegvesen.ixn.federation.api.v1_0.Constants.CAM;

@Command(name="send", description = "")
public class Send implements Callable<Integer> {

    @ParentCommand
    SubscriptionsCommand parentCommand;

    @Option(names = {"-f", "--filename"}, description = "The message json file", required = true)
    File messageFile;

    @Override
    public Integer call() throws Exception {

        System.out.printf("Sending message from file %s%n",messageFile);
        ObjectMapper mapper = new ObjectMapper();
        Messages messages = mapper.readValue(messageFile, Messages.class);
        String queueName = "";
        try (Source source = new Source(parentCommand.getParent().getUrl(), queueName, parentCommand.getParent().createSSLContext())) {
            source.start();
            for (Message message : messages.getMessages()) {
                MessageBuilder messageBuilder = source.createMessageBuilder();
                switch (message){
                    case DenmMessage ignored -> {
                        messageBuilder
                                .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
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
                                .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                                .messageType(IVIM)
                                .iviType(((IvimMessage) message).getIviType())
                                .pictogramCategoryCode(((IvimMessage) message).getPictogramCategoryCode())
                                .iviContainer(((IvimMessage) message).getIviContainer());
                    }
                    case SpatemMessage ignored -> {
                        messageBuilder
                                .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                                .messageType(SPATEM)
                                .id(((SpatemMessage) message).getId())
                                .name(((SpatemMessage) message).getName());
                    }
                    case MapemMessage ignored -> {
                        messageBuilder
                                .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                                .messageType(MAPEM)
                                .id(((MapemMessage) message).getId())
                                .name(((MapemMessage) message).getName());
                    }
                    case SsemMessage ignored -> {
                        messageBuilder
                                .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                                .messageType(SSEM)
                                .id(((SsemMessage) message).getId());
                    }
                    case SremMessage ignored -> {
                        messageBuilder
                                .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                                .messageType(SREM)
                                .id(((SremMessage) message).getId());
                    }
                    case CamMessage ignored -> {
                        messageBuilder
                                .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                                .messageType(CAM)
                                .stationType(((CamMessage) message).getStationType())
                                .vehicleRole(((CamMessage) message).getVehicleRole());
                    }

                    default ->  throw new Exception("Message is not of valid messagetype");
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
}
