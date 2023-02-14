package no.vegvesen.ixn;


import no.vegvesen.ixn.federation.api.v1_0.Constants;
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

    @Option(names = {"-k","--keystorepath"}, description = "Path to the service provider p12 keystore")
    private String keystorePath;

    @Option(names = {"-s","--keystorepassword"}, description = "The password of the service provider keystore")
    String keystorePassword;

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

    private SSLContext createSSLContext() {
        KeystoreDetails keystoreDetails = new KeystoreDetails(keystorePath,
                keystorePassword,
                KeystoreType.PKCS12);
        KeystoreDetails trustStoreDetails = new KeystoreDetails(trustStorePath,
                trustStorePassword,KeystoreType.JKS);
        return SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);
    }

}
