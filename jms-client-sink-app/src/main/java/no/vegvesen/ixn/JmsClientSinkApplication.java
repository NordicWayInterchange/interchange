package no.vegvesen.ixn;

import java.util.concurrent.Callable;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import picocli.CommandLine;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import javax.net.ssl.SSLContext;

@Command(name = "jmsclientsink", description = "JMS Client Sink Application", subcommands = {
        JmsClientSinkApplication.ReceiveMessages.class
})
public class JmsClientSinkApplication implements Callable<Integer> {

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

    @Command(name = "receivemessages", description = "Get the messages")
    static class ReceiveMessages implements Callable<Integer> {

        @ParentCommand
        JmsClientSinkApplication parentCommand;

        @Override
        public Integer call() throws Exception {
            Sink sink = parentCommand.createClient();
            System.out.println(String.format("Listening for messages from queue [%s] on server [%s]", parentCommand.queueName, parentCommand.url));
            sink.start();
            return 0;
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new JmsClientSinkApplication()).execute(args);
        //System.exit(exitCode); Removed this to be sure that the sink is listening for messages.
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

    public Sink createClient() { return new Sink(url, queueName, createSSLContext());}
}
