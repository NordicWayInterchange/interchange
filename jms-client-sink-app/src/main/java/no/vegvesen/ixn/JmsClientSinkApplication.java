package no.vegvesen.ixn;

import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import picocli.CommandLine;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.net.ssl.SSLContext;

@Command(name = "jmsclientsink",
        description = "JMS Client Sink Application",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        subcommands = {
                JmsClientSinkApplication.ReceiveMessages.class,
                JmsClientSinkApplication.CountMessages.class,
                JmsClientSinkApplication.DrainMessages.class
        })
public class JmsClientSinkApplication implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "URL" ,description = "The url to the AMQP host to connect to")
    private String url;


    @Option(names = {"-k","--keystorepath"}, required = true, description = "Path to the service provider p12 keystore")
    private Path keystorePath;

    @Option(names = {"-s","--keystorepassword"}, required = true, description = "The password of the service provider keystore")
    String keystorePassword;

    @Option(names = {"-t","--truststorepath"}, required = true, description = "The path of the jks trust store")
    Path trustStorePath;

    @Option(names = {"-w","--truststorepassword"}, required = true, description = "The password of the jks trust store")
    String trustStorePassword;

    @Command(name = "receivemessages", description = "Receive messages and print them to stdout")
    static class ReceiveMessages implements Callable<Integer> {

        @Parameters(index = "0", paramLabel = "QUEUE" ,description = "The queueName to connect to")
        private String queueName;
        @ParentCommand
        JmsClientSinkApplication parentCommand;

        @Override
        public Integer call() throws Exception {
            Sink sink = parentCommand.createClient(queueName);
            System.out.println(String.format("Listening for messages from queue [%s] on server [%s]", queueName, parentCommand.url));
            sink.start();
            return 0;
        }
    }

    @Command(name = "countmessages", description = "Count number of messages until client is killed by Ctrl+C")
    static class CountMessages implements Callable<Integer> {

        @Parameters(index = "0", paramLabel = "QUEUE" ,description = "The queueName to connect to")
        private String queueName;

        @ParentCommand
        JmsClientSinkApplication parentCommand;
        @Override
        public Integer call() throws Exception {
            AtomicLong counter = new AtomicLong();
            Runtime.getRuntime().addShutdownHook(new Thread(
                    () -> {
                        System.out.println(String.format("Receivied %d messages", counter.get()));
                    }
            ));
            Sink sink = parentCommand.createClientWithListener(queueName, message -> counter.incrementAndGet());
            sink.start();
            return 0;
        }
    }

    @Command(name = "drainmessages", description = "Drains the queue until there's more than 0.5 second delay")
    static class DrainMessages implements Callable<Integer> {

        @Parameters(index = "0", paramLabel = "QUEUE" ,description = "The queueName to connect to")
        private String queueName;
        @ParentCommand
        JmsClientSinkApplication parentCommand;


        @Override
        public Integer call() throws Exception {
            Sink sink = parentCommand.createClient(queueName);
            MessageConsumer consumer = sink.createConsumer();
            while (consumer.receive(500) != null); //drains the queue
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
        KeystoreDetails keystoreDetails = new KeystoreDetails(keystorePath.toString(),
                keystorePassword,
                KeystoreType.PKCS12);
        KeystoreDetails trustStoreDetails = new KeystoreDetails(trustStorePath.toString(),
                trustStorePassword,KeystoreType.JKS);
        return SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);
    }

    public Sink createClient(String queueName) { return new Sink(url, queueName, createSSLContext());}

    public Sink createClientWithListener(String queueName, MessageListener listener) {
        return new Sink(url,queueName,createSSLContext(),listener);
    }
}
