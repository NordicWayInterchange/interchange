package no.vegvesen.ixn.federation.serviceproviderclient.command.jms;

import no.vegvesen.ixn.Sink;
import static picocli.CommandLine.*;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

@Command(name = "count", description = "Count number of messages until client is killed by Ctrl+C")
public class CountMessages implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "QUEUE", description = "The queueName to connect to")
    private String queueName;

    @ParentCommand
    MessagesCommand parentCommand;

    //TODO will this wait for messages?
    @Override
    public Integer call() throws Exception {
        AtomicLong counter = new AtomicLong();
        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> {
                    System.out.println(String.format("Receivied %d messages", counter.get()));
                }
        ));
        try (Sink sink = new Sink(parentCommand.getUrl(), queueName, parentCommand.createContext(), message -> counter.incrementAndGet())) {
            sink.start();
        }
        return 0;
    }
}
