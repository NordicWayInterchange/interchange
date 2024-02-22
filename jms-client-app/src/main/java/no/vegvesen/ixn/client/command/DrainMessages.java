package no.vegvesen.ixn.client.command;

import no.vegvesen.ixn.Sink;
import picocli.CommandLine;

import javax.jms.MessageConsumer;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "drainmessages", description = "Drains the queue until there's more than 0.5 second delay")
public class DrainMessages implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", paramLabel = "QUEUE", description = "The queueName to connect to")
    private String queueName;
    @CommandLine.ParentCommand
    JmsTopCommand parentCommand;


    @Override
    public Integer call() throws Exception {
        try (Sink sink = new Sink(parentCommand.getUrl(), queueName, parentCommand.createContext())) {
            MessageConsumer consumer = sink.createConsumer();
            while (consumer.receive(500) != null) ; //drains the queue
        }
        return 0;
    }
}
