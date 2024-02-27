package no.vegvesen.ixn.client.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.MessageBuilder;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.message.*;
import picocli.CommandLine;
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
                MessageBuilder messageBuilder = source.createMessageBuilder()
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
                if (message instanceof DenmMessage) {
                    messageBuilder
                            .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                            .messageType(DENM)
                            .causeCode(((DenmMessage) message).getCauseCode())
                            .subCauseCode(((DenmMessage) message).getSubCauseCode());
                } else if (message instanceof DatexMessage) {
                    messageBuilder
                            .textMessage(message.getMessageText())
                            .messageType(DATEX_2)
                            .publicationType(((DatexMessage) message).getPublicationType())
                            .publicationSubType(((DatexMessage) message).getPublicationSubType());
                } else if (message instanceof IvimMessage) {
                    messageBuilder
                            .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                            .messageType(IVIM)
                            .iviType(((IvimMessage) message).getIviType())
                            .pictogramCategoryCode(((IvimMessage) message).getPictogramCategoryCode())
                            .iviContainer(((IvimMessage) message).getIviContainer());
                } else if (message instanceof SpatemMessage) {
                    messageBuilder
                            .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                            .messageType(SPATEM)
                            .id(((SpatemMessage) message).getId())
                            .name(((SpatemMessage) message).getName());
                } else if (message instanceof MapemMessage) {
                    messageBuilder
                            .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                            .messageType(MAPEM)
                            .id(((MapemMessage) message).getId())
                            .name(((MapemMessage) message).getName());
                } else if (message instanceof SsemMessage) {
                    messageBuilder
                            .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                            .messageType(SSEM)
                            .id(((SsemMessage) message).getId());
                } else if (message instanceof SremMessage) {
                    messageBuilder
                            .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                            .messageType(SREM)
                            .id(((SremMessage) message).getId());
                } else if (message instanceof CamMessage) {
                    messageBuilder
                            .bytesMessage(message.getMessageText().getBytes(StandardCharsets.UTF_8))
                            .messageType(CAM)
                            .stationType(((CamMessage) message).getStationType())
                            .vehicleRole(((CamMessage) message).getVehicleRole());
                } else {
                    throw new Exception("Message is not of valid messagetype");
                }
                source.send(messageBuilder.build());

            }
        }
        return 0;
    }
}
