package no.vegvesen.ixn;


import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import picocli.CommandLine;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

@Command(name = "jmsclientsource", description = "JMS Client Source Application", subcommands = {
        JmsClientSourceApplication.SendBytesMessage.class,
        JmsClientSourceApplication.SendTextMessage.class
})
public class JmsClientSourceApplication implements Callable<Integer> {

    @Parameters(index = "0", description = "The url to the AMQP client")
    private String url;

    @Parameters(index = "1", description = "The queueName for the Service Provider")
    private String queueName;

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

    @Command(name = "sendbytesmessage", description = "Send a byte message")
    static class SendBytesMessage implements Callable<Integer> {

        @ParentCommand
        JmsClientSourceApplication parentCommand;

        @Override
        public Integer call() throws Exception {
            try(Source source = parentCommand.createClient()){
                source.start();
                String messageText = "{Tester med meg selv hehehehe}";
                byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);

                source.sendNonPersistentMessage(source.createMessageBuilder()
                        .bytesMessage(bytemessage)
                        .userId("anna")
                        .messageType(Constants.DENM)
                        .publisherId("NO00001")
                        .originatingCountry("NO")
                        .protocolVersion("DENM:1.2.2")
                        .quadTreeTiles(",12004")
                        .causeCode("5")
                        .subCauseCode("56")
                        .build());
            } catch (Exception e){
                e.printStackTrace();
            }
            return 0;
        }
    }

    @Command(name = "sendtextmessage", description = "Send a text message")
    static class SendTextMessage implements Callable<Integer> {
        @ParentCommand
        JmsClientSourceApplication parentCommand;

        @Override
        public Integer call() throws Exception {
            try(Source source = parentCommand.createClient()){
                source.start();
                String messageText = "{Dette er en datex2 melding hehehe}";

                source.sendNonPersistentMessage(source.createMessageBuilder()
                    .textMessage(messageText)
                    .userId("anna")
                    .datex2MessageType()
                    .publisherId("NO00001")
                    .originatingCountry("NO")
                    .protocolVersion("DATEX2;2.3")
                    .publicationType("SituationPublication")
                    .quadTreeTiles(",12004")
                    .build());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new JmsClientSourceApplication()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
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

    public Source createClient() {
        return new Source(url, queueName, createSSLContext());
    }
}
