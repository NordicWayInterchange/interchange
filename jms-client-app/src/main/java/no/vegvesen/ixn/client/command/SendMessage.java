package no.vegvesen.ixn.client.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.MessageBuilder;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.message.*;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import static no.vegvesen.ixn.federation.api.v1_0.Constants.*;

@Command(name = "sendmessage", description = "Sending a message from JSON")
public class SendMessage implements Callable<Integer> {

    @Parameters(paramLabel = "QUEUE", description = "The queueName to connect to")
    private String queueName;

    @Option(names = {"-f", "--filename"}, description = "The message json file", required = true)
    File messageFile;

    @ParentCommand
    JmsTopCommand parentCommand;

    @Override
    public Integer call() throws Exception {
        System.out.printf("Sending message from file %s%n",messageFile);
        ObjectMapper mapper = new ObjectMapper();
        Messages messages = mapper.readValue(messageFile, Messages.class);
        try (Source source = new Source(parentCommand.getUrl(), queueName, parentCommand.createContext())) {
            source.start();
            for (Message message : messages.getMessages()) {
                MessageBuilder messageBuilder = source.createMessageBuilder();
                if (message instanceof DenmMessage denmMessage) {
                    messageBuilder
                            .bytesMessage(denmMessage.getMessageText().getBytes(StandardCharsets.UTF_8))
                            .messageType(DENM)
                            .causeCode(denmMessage.getCauseCode())
                            .subCauseCode(denmMessage.getSubCauseCode());
                } else if (message instanceof DatexMessage datexMessage) {
                    messageBuilder
                            .textMessage(message.getMessageText())
                            .messageType(DATEX_2)
                            .publicationType(datexMessage.getPublicationType())
                            .publicationSubType(datexMessage.getPublicationSubType())
                            .publisherName(datexMessage.getPublisherName());
                } else if (message instanceof IvimMessage ivimMessage) {
                    messageBuilder
                            .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                            .messageType(IVIM)
                            .iviType(ivimMessage.getIviType())
                            .pictogramCategoryCode(ivimMessage.getPictogramCategoryCode())
                            .iviContainer(ivimMessage.getIviContainer());
                } else if (message instanceof SpatemMessage spatemMessage) {
                    messageBuilder
                            .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                            .messageType(SPATEM)
                            .id(spatemMessage.getId())
                            .name(spatemMessage.getName());
                } else if (message instanceof MapemMessage mapemMessage) {
                    messageBuilder
                            .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                            .messageType(MAPEM)
                            .id(mapemMessage.getId())
                            .name(mapemMessage.getName());
                } else if (message instanceof SsemMessage ssemMessage) {
                    messageBuilder
                            .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                            .messageType(SSEM)
                            .id(ssemMessage.getId());
                } else if (message instanceof SremMessage ssremMessage) {
                    messageBuilder
                            .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                            .messageType(SREM)
                            .id(ssremMessage.getId());
                } else if (message instanceof CamMessage camMessage) {
                    messageBuilder
                            .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                            .messageType(CAM)
                            .stationType(camMessage.getStationType())
                            .vehicleRole(camMessage.getVehicleRole());
                } else {
                    throw new Exception("Message is not of valid messagetype");
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
