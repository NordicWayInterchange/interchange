package no.vegvesen.ixn.client.command;

import no.vegvesen.ixn.Sink;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "receivemessages", description = "Receive messages and print them to stdout")
public class ReceiveMessages implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", paramLabel = "QUEUE", description = "The queueName to connect to")
    private String queueName;
    @CommandLine.ParentCommand
    JmsTopCommand parentCommand;

    @Override
    public Integer call() throws Exception {
        try (Sink sink = new Sink(parentCommand.getUrl(), queueName, parentCommand.createContext())) {
            System.out.println(String.format("Listening for messages from queue [%s] on server [%s]", queueName, parentCommand.getUrl()));
            sink.start();
        }
        return 0;
    }
}
