package no.vegvesen.ixn;

import no.vegvesen.ixn.client.command.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.concurrent.Callable;


@Command(name = "jmsclient",
        description = "Jms Client Application",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        subcommands = {
                SendMessage.class,
                ReceiveMessages.class,
                CountMessages.class
        },
        mixinStandardHelpOptions = true)
public class JmsClientApplication implements JmsTopCommand {
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

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public Path getKeystorePath() {
        return keystorePath;
    }

    @Override
    public String getKeystorePassword() {
        return keystorePassword;
    }

    @Override
    public Path getTrustStorePath() {
        return trustStorePath;
    }

    @Override
    public String getTrustStorePassword() {
        return trustStorePassword;
    }


    public static void main(String[] args) {
        int exitCode = new CommandLine(new JmsClientApplication()).execute(args);
        //System.exit(exitCode); Removed this to be sure that the sink is listening for messages.
    }

}
