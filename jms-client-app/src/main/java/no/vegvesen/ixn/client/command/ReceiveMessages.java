package no.vegvesen.ixn.client.command;

import no.vegvesen.ixn.Sink;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

@Command(name = "receivemessages", description = "Receive messages and print them to stdout")
public class ReceiveMessages implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "QUEUE", description = "The queueName to connect to")
    private String queueName;
    @ParentCommand
    JmsTopCommand parentCommand;

    private CountDownLatch counter = new CountDownLatch(1);

    @Override
    public Integer call() throws Exception {
        System.out.println(String.format("Listening for messages from queue [%s] on server [%s]", queueName, parentCommand.getUrl()));
        try (Sink sink = new Sink(parentCommand.getUrl(), queueName, parentCommand.createContext())) {
            sink.start();
            counter.await();
        }
        return 0;
    }

    public CountDownLatch getCounter() {
        return counter;
    }
}
