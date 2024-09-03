package no.vegvesen.ixn.federation.serviceproviderclient.command.jms;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.MessageBuilder;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.federation.serviceproviderclient.messages.*;

import static picocli.CommandLine.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.concurrent.Callable;

import static no.vegvesen.ixn.federation.api.v1_0.Constants.*;

@Command(name = "send", description = "Sending a message from JSON")
public class SendMessage implements Callable<Integer> {

    @Parameters(paramLabel = "QUEUE", description = "The queueName to connect to")
    private String queueName;

    @Option(names = {"-f", "--filename"}, description = "The message json file", required = true)
    File messageFile;

    @Option(names = {"-b", "--binary"}, description = "", required = false)
    boolean binary;

    @ParentCommand
    MessagesCommand parentCommand;

    @Override
    public Integer call() throws Exception {
        System.out.printf("Sending message from file %s%n",messageFile);
        ObjectMapper mapper = new ObjectMapper();
        validateInput();
            Messages messages = mapper.readValue(messageFile, Messages.class);
            try (Source source = new Source(parentCommand.getUrl(), queueName, parentCommand.createContext())) {
                source.start();
                for (Message message : messages.getMessages()) {
                    MessageBuilder messageBuilder = source.createMessageBuilder();
                    try {
                        switch (message){
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
                    catch(NoSuchFileException e){
                        throw new Exception(String.format("File %s not found", message.getFileName()));
                    }
                }
            }

        return 0;
    }

    private void validateInput() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Messages messages = mapper.readValue(messageFile, Messages.class);
        for(Message message : messages.getMessages()){
            if(binary) {
                if(message.getMessageType().equals(DATEX_2)){
                    throw new Exception("DATEX messages can not be sent binary.");
                }
                if (message.getFile() == null) {
                    throw new Exception("Message does not contain file");
                }
            }
            else{
                if(message.getMessageText() == null){
                    throw new Exception("Message does not contain messageText");
                }
            }
        }
    }

    private static byte[] convertFileToByteArray(String fileName) throws IOException {
        File file = new File(fileName);
        byte [] bytes = Files.readAllBytes(file.toPath());
        return bytes;
    }
}
