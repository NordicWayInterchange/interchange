package no.vegvesen.ixn;

import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import picocli.CommandLine;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import com.fasterxml.jackson.databind.ObjectMapper;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;


@Command(name = "jmssource", description = "Jms Client Source", showAtFileInUsageHelp = true, subcommands = {
        JmsSourceApplication.SendTextMessage.class,
        JmsSourceApplication.SendDenmMessage.class
})
public class JmsSourceApplication implements Callable<Integer> {

    @Autowired
    private static JmsSourceProperties properties;

    @Command(name = "sendtextmessage", description = "Sending multiple DATEX2 messages")
    static class SendTextMessage implements Callable<Integer> {

        @Option(names = {"-f","--filename"}, description = "The messages json file")
        File file;

        @Override
        public Integer call() throws IOException {
            ObjectMapper mapper = new ObjectMapper();
            MessageListApi messages = mapper.readValue(file, MessageListApi.class);
            System.out.println(mapper.writeValueAsString(messages));
            try{Source s = new Source(properties.getUrl(), properties.getSendQueue(), createSSLContext());
                System.out.println("Started source");
                s.start();
                for (MessageApi message : messages.getMessages()) {
                    s.sendDatexMessageFromFile(message.getMessageText(),
                            message.getQuadTreeTiles(),
                            message.getOriginatingCountry(),
                            message.getPublisherId(),
                            message.getLatitude(),
                            message.getLongitude(),
                            message.getProtocolVersion(),
                            message.getServiceType(),
                            message.getPublicationType(),
                            message.getPublicationSubType());
                }
                s.close();
            } catch (JMSException e) {
                e.printStackTrace();
            } catch (NamingException e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    @Command(name = "senddenmmessage", description = "Sending multiple DENM messages")
    static class SendDenmMessage implements Callable<Integer> {

        @Override
        public Integer call() {
            return 0;
        }
    }

/*    public static void main(String[] args) {
        int exitCode = new CommandLine(new JmsSourceApplication()).execute(args);
        System.exit(exitCode);
    }*/

    @Override
    public Integer call() {
        return 0;
    }

    private static SSLContext createSSLContext() {
        KeystoreDetails keystoreDetails = new KeystoreDetails(properties.getKeystorePath(),
                properties.getKeystorePass(),
                KeystoreType.PKCS12, properties.getKeyPass());
        KeystoreDetails trustStoreDetails = new KeystoreDetails(properties.getTrustStorepath(),
                properties.getTrustStorepass(),KeystoreType.JKS);
        return SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);
    }
}
