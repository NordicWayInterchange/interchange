package no.vegvesen.ixn;


import no.vegvesen.ixn.client.command.JmsTopCommand;
import no.vegvesen.ixn.client.command.SendMessage;
import no.vegvesen.ixn.client.command.SendPredefinedMessage;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.Callable;


@Command(name = "jmsclientsource",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        showAtFileInUsageHelp = true,
        description = "JMS Client Source Application",
        subcommands = {
                SendPredefinedMessage.class,
                SendMessage.class
        },
        mixinStandardHelpOptions = true)
public class JmsClientSourceApplication implements JmsTopCommand {

    @Parameters(index = "0", paramLabel = "URL", description = "The AMQPS url to connect to")
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
        int exitCode = new CommandLine(new JmsClientSourceApplication()).execute(args);
        System.exit(exitCode);
    }


}
