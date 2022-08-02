package no.vegvesen.ixn;


import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

/*
TODO this app does not work as is. It should have a command per message type, taking positional parameters after.

 */
@Command(name = "jmsclientsource", description = "JMS Client Source Application")
public class JmsClientSourceApplication implements Callable<Integer> {

    @Parameters(index = "0", description = "The url to the AMQP client")
    private String url;

    @Parameters(index = "1", description = "The queueName for the Service Provider")
    private String queueName;

    @Parameters(index = "2", description = "The user id of the sender")
    String userid;

    @Parameters(index = "3", description = "The publisher id of the sender")
    String publisherId;

    @Parameters(index = "4", description = "The type of the message to be sent")
    String messageType;

    @Parameters(index = "5", description = "The originating country of the message")
    String originatingCountry;

    @Parameters(index = "6", description = "The protocol version of the message")
    String protocolVersion;

    @Parameters(index = "7", description = "The quadtree tiles of the message")
    String quadTreeTiles;

    @Parameters(index = "8", description = "The message to be sent")
    String message;

    @Option(names = {"-k","--keystorepath"}, description = "Path to the service provider p12 keystore")
    private String keystorePath;

    @Option(names = {"-s","--keystorepassword"}, description = "The password of the service provider keystore")
    String keystorePassword;

    @Option(names = {"-p", "--keypassword"}, description = "The password of the service provider key")
    String keyPassword;

    @Option(names = {"-t","--truststorepath"}, description = "The path of the jks trust store")
    String trustStorePath;

    @Option(names = {"-w","--truststorepassword"}, description = "The password of the jks trust store")
    String trustStorePassword;


    public static void main(String[] args) {
        int exitCode = new CommandLine(new JmsClientSourceApplication()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        try(Source source = new Source(url, queueName, createSSLContext())){
            source.start();
            String messageText = message;
            byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);

            source.send(source.createMessageBuilder()
                    .bytesMessage(bytemessage)
                    .userId(userid)
                    .messageType(messageType)
                    .publisherId(publisherId)
                    .originatingCountry(originatingCountry)
                    .protocolVersion(protocolVersion)
                    .quadTreeTiles(quadTreeTiles)
                    .build());
        }
        return 0;
    }

    private SSLContext createSSLContext() {
        KeystoreDetails keystoreDetails = new KeystoreDetails(keystorePath,
                keystorePassword,
                KeystoreType.PKCS12, keyPassword);
        KeystoreDetails trustStoreDetails = new KeystoreDetails(trustStorePath,
                trustStorePassword,KeystoreType.JKS);
        return SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);
    }

}
