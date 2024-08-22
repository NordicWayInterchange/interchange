package no.vegvesen.ixn.client.command;

import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import picocli.CommandLine;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import static no.vegvesen.ixn.federation.api.v1_0.Constants.DENM;

@CommandLine.Command(name = "sendpredefinedmessage", description = "Sending a message that i predefined")
public class SendPredefinedMessage implements Callable<Integer> {

    @CommandLine.ParentCommand
    JmsTopCommand parentCommand;

    @CommandLine.Parameters(paramLabel = "QUEUE", description = "The queueName to connect to")
    private String queueName;

    @Override
    public Integer call() throws Exception {
        KeystoreDetails keystoreDetails = new KeystoreDetails(parentCommand.getKeystorePath().toString(),
                parentCommand.getKeystorePassword(),
                KeystoreType.PKCS12);
        KeystoreDetails trustStoreDetails = new KeystoreDetails(parentCommand.getTrustStorePath().toString(),
                parentCommand.getTrustStorePassword(), KeystoreType.JKS);
        try (Source source = new Source(parentCommand.getUrl(), queueName, SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails))) {
            source.start();
            String messageText = "This is a test!";
            source.send(source.createMessageBuilder()
                    .bytesMessage(messageText.getBytes(StandardCharsets.UTF_8))
                    .userId("anna")
                    .messageType(DENM)
                    .publisherId("NO00001")
                    .originatingCountry("NO")
                    .protocolVersion("DENM:1.2.2")
                    .quadTreeTiles(",12003,")
                    .causeCode(6)
                    .subCauseCode(61)
                    .build());
            System.out.println(messageText);
        }
        return 0;
    }
}
