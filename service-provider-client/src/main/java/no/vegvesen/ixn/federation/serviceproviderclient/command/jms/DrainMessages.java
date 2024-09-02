package no.vegvesen.ixn.federation.serviceproviderclient.command.jms;

import jakarta.jms.MessageConsumer;
import no.vegvesen.ixn.Sink;
import static picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Command(name = "drain", description = "Drains the queue until there's more than 0.5 second delay")
public class DrainMessages implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "QUEUE", description = "The queueName to connect to")
    private String queueName;

    @ParentCommand
    MessagesCommand parentCommand;


    @Override
    public Integer call() throws Exception {
        try (Sink sink = new Sink(parentCommand.getUrl(), queueName, parentCommand.createContext())) {
            MessageConsumer consumer = sink.createConsumer();
            while (consumer.receive(500) != null) ; //drains the queue
        }
        return 0;
    }
}
