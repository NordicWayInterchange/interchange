package no.vegvesen.ixn.client.command;

import no.vegvesen.ixn.Sink;
import picocli.CommandLine;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

@CommandLine.Command(name = "countmessages", description = "Count number of messages until client is killed by Ctrl+C")
public class CountMessages implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", paramLabel = "QUEUE", description = "The queueName to connect to")
    private String queueName;

    @CommandLine.ParentCommand
    JmsTopCommand parentCommand;

    @Override
    public Integer call() throws Exception {
        AtomicLong counter = new AtomicLong();
        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> {
                    System.out.println(String.format("Receivied %d messages", counter.get()));
                }
        ));
        try (Sink sink = new Sink(parentCommand.getUrl(), queueName, parentCommand.createContext())) {
            sink.start();
        }
        return 0;
    }
}
