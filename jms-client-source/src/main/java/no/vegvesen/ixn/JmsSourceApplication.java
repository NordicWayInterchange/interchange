package no.vegvesen.ixn;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import picocli.CommandLine;

import javax.net.ssl.SSLContext;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;

@Command(name = "jmssource", description = "Jms Client Source", showAtFileInUsageHelp = true, subcommands = {
        JmsSourceApplication.SendTextMessage.class,
        JmsSourceApplication.SendDenmMessage.class
})
public class JmsSourceApplication implements Callable<Integer> {

    @Autowired
    private JmsSourceProperties properties;

    @Command()
    static class SendTextMessage implements Callable<Integer> {

        @Override
        public Integer call() {
            return 0;
        }
    }

    @Command()
    static class SendDenmMessage implements Callable<Integer> {

        @Override
        public Integer call() {
            return 0;
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new JmsSourceApplication()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        return 0;
    }

    private SSLContext createSSLContext() {
        KeystoreDetails keystoreDetails = new KeystoreDetails(properties.getKeystorePath(),
                properties.getKeystorePass(),
                KeystoreType.PKCS12, properties.getKeyPass());
        KeystoreDetails trustStoreDetails = new KeystoreDetails(properties.getTrustStorepath(),
                properties.getTrustStorepass(),KeystoreType.JKS);
        return SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);
    }
}
