package no.vegvesen.ixn;


import no.vegvesen.ixn.federation.api.v1_0.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.message.*;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.apache.qpid.jms.message.JmsMessage;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import jakarta.jms.JMSException;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/*
TODO this app does not work as is. It should have a command per message type, taking positional parameters after.

 */
@Command(name = "jmsclientsource",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        showAtFileInUsageHelp = true,
        description = "JMS Client Source Application",
        subcommands = {
                JmsClientSourceApplication.SendPredefinedMessage.class,
                JmsClientSourceApplication.SendMessage.class
        })
public class JmsClientSourceApplication implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "URL", description = "The AMQPS url to connect to")
    private String url;

    @Parameters(index = "1", paramLabel =  "QUEUE",description = "The queueName to connect to")
    private String queueName;

    @Option(names = {"-k","--keystorepath"}, required = true, description = "Path to the service provider p12 keystore")
    private Path keystorePath;

    @Option(names = {"-s","--keystorepassword"}, required = true, description = "The password of the service provider keystore")
    String keystorePassword;

    @Option(names = {"-t","--truststorepath"}, required = true, description = "The path of the jks trust store")
    Path trustStorePath;

    @Option(names = {"-w","--truststorepassword"}, required = true, description = "The password of the jks trust store")
    String trustStorePassword;

    @Command(name = "sendpredefinedmessage", description = "Sending a message that i predefined")
    static class SendPredefinedMessage implements Callable<Integer> {

        @ParentCommand
        JmsClientSourceApplication parentCommand;

        @Override
        public Integer call() throws Exception {
            try(Source source = new Source(parentCommand.url, parentCommand.queueName, parentCommand.createSSLContext())){
                source.start();
                String messageText = "This is a test!";
                byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);

                source.send(source.createMessageBuilder()
                        .bytesMessage(bytemessage)
                        .userId("anna")
                        .messageType(Constants.DENM)
                        .publisherId("NO00001")
                        .originatingCountry("NO")
                        .protocolVersion("DENM:1.2.2")
                        .quadTreeTiles(",12003,")
                        .causeCode("6")
                        .subCauseCode("61")
                        .build());
                System.out.println(messageText);
            }
            return 0;
        }
    }

    @Command(name = "sendmessage", description = "Sending a message from JSON")
    static class SendMessage implements Callable<Integer> {

        @Option(names = {"-f","--filename"}, description = "The message json file")
        File messageFile;

        @ParentCommand
        JmsClientSourceApplication parentCommand;

        @Override
        public Integer call() throws Exception {
            ObjectMapper mapper = new ObjectMapper();
            Messages messages = mapper.readValue(messageFile, Messages.class);
            try(Source source = new Source(parentCommand.url, parentCommand.queueName, parentCommand.createSSLContext())){
                source.start();
                for (Message message : messages.getMessages()) {
                    if (message instanceof DenmMessage) {
                        source.send(parentCommand.createDenmMessage(source.createMessageBuilder(), (DenmMessage) message));
                    } else if (message instanceof  DatexMessage) {
                        source.send(parentCommand.createDatexMessage(source.createMessageBuilder(), (DatexMessage) message));
                    } else if (message instanceof IvimMessage) {
                        source.send(parentCommand.createIvimMessage(source.createMessageBuilder(), (IvimMessage) message));
                    } else if (message instanceof SpatemMessage) {
                        source.send(parentCommand.createSpatemMessage(source.createMessageBuilder(), (SpatemMessage) message));
                    } else if (message instanceof MapemMessage) {
                        source.send(parentCommand.createMapemMessage(source.createMessageBuilder(), (MapemMessage) message));
                    } else if (message instanceof SsemMessage) {
                        source.send(parentCommand.createSsemMessage(source.createMessageBuilder(), (SsemMessage) message));
                    } else if (message instanceof SremMessage) {
                        source.send(parentCommand.createSremMessage(source.createMessageBuilder(), (SremMessage) message));
                    } else if (message instanceof CamMessage) {
                        source.send(parentCommand.createCamMessage(source.createMessageBuilder(), (CamMessage) message));
                    } else {
                        throw new Exception("Message is not of valid messagetype");
                    }
                }
            }
            return 0;
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new JmsClientSourceApplication()).execute(args);
        System.exit(exitCode);
    }

    private JmsMessage createDenmMessage(MessageBuilder messageBuilder, DenmMessage message) throws JMSException {
        byte[] bytemessage = message.getMessageText().getBytes(StandardCharsets.UTF_8);
        return messageBuilder
                .bytesMessage(bytemessage)
                .userId(message.getUserId())
                .messageType(Constants.DENM)
                .publisherId(message.getPublisherId())
                .publicationId(message.getPublicationId())
                .originatingCountry(message.getOriginatingCountry())
                .protocolVersion(message.getProtocolVersion())
                .serviceType(message.getServiceType())
                .baselineVersion(message.getBaselineVersion())
                .longitude(message.getLongitude())
                .latitude(message.getLatitude())
                .quadTreeTiles(message.getQuadTree())
                .shardId(message.getShardId())
                .shardCount(message.getShardCount())
                .causeCode(message.getCauseCode().toString())
                .subCauseCode(message.getSubCauseCode().toString())
                .build();
    }

    private JmsMessage createDatexMessage(MessageBuilder messageBuilder, DatexMessage message) throws JMSException {
        String textMessage = message.getMessageText();
        return messageBuilder
                .textMessage(textMessage)
                .userId(message.getUserId())
                .messageType(Constants.DATEX_2)
                .publisherId(message.getPublisherId())
                .publicationId(message.getPublicationId())
                .originatingCountry(message.getOriginatingCountry())
                .protocolVersion(message.getProtocolVersion())
                .serviceType(message.getServiceType())
                .baselineVersion(message.getBaselineVersion())
                .longitude(message.getLongitude())
                .latitude(message.getLatitude())
                .quadTreeTiles(message.getQuadTree())
                .shardId(message.getShardId())
                .shardCount(message.getShardCount())
                .publicationType(message.getPublicationType())
                .publicationSubType(message.getPublicationSubType())
                .build();
    }

    private JmsMessage createIvimMessage(MessageBuilder messageBuilder, IvimMessage message) throws JMSException {
        byte[] bytemessage = message.getMessageText().getBytes(StandardCharsets.UTF_8);
        return messageBuilder
                .bytesMessage(bytemessage)
                .userId(message.getUserId())
                .messageType(Constants.IVIM)
                .publisherId(message.getPublisherId())
                .publicationId(message.getPublicationId())
                .originatingCountry(message.getOriginatingCountry())
                .protocolVersion(message.getProtocolVersion())
                .serviceType(message.getServiceType())
                .baselineVersion(message.getBaselineVersion())
                .longitude(message.getLongitude())
                .latitude(message.getLatitude())
                .quadTreeTiles(message.getQuadTree())
                .shardId(message.getShardId())
                .shardCount(message.getShardCount())
                .iviType(message.getIviType())
                .pictogramCategoryCode(message.getPictogramCategoryCode())
                .iviContainer(message.getIviContainer())
                .build();
    }

    private JmsMessage createSpatemMessage(MessageBuilder messageBuilder, SpatemMessage message) throws JMSException {
        byte[] bytemessage = message.getMessageText().getBytes(StandardCharsets.UTF_8);
        return messageBuilder
                .bytesMessage(bytemessage)
                .userId(message.getUserId())
                .messageType(Constants.SPATEM)
                .publisherId(message.getPublisherId())
                .publicationId(message.getPublicationId())
                .originatingCountry(message.getOriginatingCountry())
                .protocolVersion(message.getProtocolVersion())
                .serviceType(message.getServiceType())
                .baselineVersion(message.getBaselineVersion())
                .longitude(message.getLongitude())
                .latitude(message.getLatitude())
                .quadTreeTiles(message.getQuadTree())
                .shardId(message.getShardId())
                .shardCount(message.getShardCount())
                .id(message.getId())
                .name(message.getName())
                .build();
    }

    private JmsMessage createMapemMessage(MessageBuilder messageBuilder, MapemMessage message) throws JMSException {
        byte[] bytemessage = message.getMessageText().getBytes(StandardCharsets.UTF_8);
        return messageBuilder
                .bytesMessage(bytemessage)
                .userId(message.getUserId())
                .messageType(Constants.MAPEM)
                .publisherId(message.getPublisherId())
                .publicationId(message.getPublicationId())
                .originatingCountry(message.getOriginatingCountry())
                .protocolVersion(message.getProtocolVersion())
                .serviceType(message.getServiceType())
                .baselineVersion(message.getBaselineVersion())
                .longitude(message.getLongitude())
                .latitude(message.getLatitude())
                .quadTreeTiles(message.getQuadTree())
                .shardId(message.getShardId())
                .shardCount(message.getShardCount())
                .id(message.getId())
                .name(message.getName())
                .build();
    }

    private JmsMessage createSsemMessage(MessageBuilder messageBuilder, SsemMessage message) throws JMSException {
        byte[] bytemessage = message.getMessageText().getBytes(StandardCharsets.UTF_8);
        return messageBuilder
                .bytesMessage(bytemessage)
                .userId(message.getUserId())
                .messageType(Constants.SSEM)
                .publisherId(message.getPublisherId())
                .publicationId(message.getPublicationId())
                .originatingCountry(message.getOriginatingCountry())
                .protocolVersion(message.getProtocolVersion())
                .serviceType(message.getServiceType())
                .baselineVersion(message.getBaselineVersion())
                .longitude(message.getLongitude())
                .latitude(message.getLatitude())
                .quadTreeTiles(message.getQuadTree())
                .shardId(message.getShardId())
                .shardCount(message.getShardCount())
                .id(message.getId())
                .build();
    }

    private JmsMessage createSremMessage(MessageBuilder messageBuilder, SremMessage message) throws JMSException {
        byte[] bytemessage = message.getMessageText().getBytes(StandardCharsets.UTF_8);
        return messageBuilder
                .bytesMessage(bytemessage)
                .userId(message.getUserId())
                .messageType(Constants.SREM)
                .publisherId(message.getPublisherId())
                .publicationId(message.getPublicationId())
                .originatingCountry(message.getOriginatingCountry())
                .protocolVersion(message.getProtocolVersion())
                .serviceType(message.getServiceType())
                .baselineVersion(message.getBaselineVersion())
                .longitude(message.getLongitude())
                .latitude(message.getLatitude())
                .quadTreeTiles(message.getQuadTree())
                .shardId(message.getShardId())
                .shardCount(message.getShardCount())
                .id(message.getId())
                .build();
    }

    private JmsMessage createCamMessage(MessageBuilder messageBuilder, CamMessage message) throws JMSException {
        byte[] bytemessage = message.getMessageText().getBytes(StandardCharsets.UTF_8);
        return messageBuilder
                .bytesMessage(bytemessage)
                .userId(message.getUserId())
                .messageType(Constants.CAM)
                .publisherId(message.getPublisherId())
                .publicationId(message.getPublicationId())
                .originatingCountry(message.getOriginatingCountry())
                .protocolVersion(message.getProtocolVersion())
                .serviceType(message.getServiceType())
                .baselineVersion(message.getBaselineVersion())
                .longitude(message.getLongitude())
                .latitude(message.getLatitude())
                .quadTreeTiles(message.getQuadTree())
                .shardId(message.getShardId())
                .shardCount(message.getShardCount())
                .stationType(message.getStationType())
                .vehicleRole(message.getVehicleRole())
                .build();
    }

    @Override
    public Integer call() {
        return 0;
    }

    private SSLContext createSSLContext() {
        KeystoreDetails keystoreDetails = new KeystoreDetails(keystorePath.toString(),
                keystorePassword,
                KeystoreType.PKCS12);
        KeystoreDetails trustStoreDetails = new KeystoreDetails(trustStorePath.toString(),
                trustStorePassword,KeystoreType.JKS);
        return SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);
    }

}
