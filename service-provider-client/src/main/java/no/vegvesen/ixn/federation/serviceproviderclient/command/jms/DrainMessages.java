package no.vegvesen.ixn.federation.serviceproviderclient.command.jms;

import jakarta.jms.MessageConsumer;
import no.vegvesen.ixn.Sink;
import picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Command(name = "drain", description = "Drains the queue until there's more than 0.5 second delay",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """
                        Example:\n
                        serviceproviderclient messages drain loc-60644a9f-ba59-480c-ade9-cfecda3784d6
                        """
        })
public class DrainMessages implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "QUEUE", description = "The queueName to connect to")
    private String queueName;

    @ParentCommand
    MessagesCommand parentCommand;

    @Override
    public Integer call() throws Exception {
        try (Sink sink = new Sink(parentCommand.getUrl(), queueName, parentCommand.createContext())) {
            MessageConsumer consumer = sink.createConsumer();
            while (consumer.receive(500) != null) ;
        }
        return 0;
    }
}
