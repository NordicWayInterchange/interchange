package no.vegvesen.ixn.client.command;

import jakarta.jms.ExceptionListener;
import no.vegvesen.ixn.Sink;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@Command(name = "receivemessages", description = "Receive messages and print them to stdout")
public class ReceiveMessages implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "QUEUE", description = "The queueName to connect to")
    private String queueName;
    @ParentCommand
    JmsTopCommand parentCommand;

    private final CountDownLatch counter = new CountDownLatch(1);

    @Override
    public Integer call() throws Exception {
        System.out.printf("Listening for messages from queue [%s] on server [%s]%n", queueName, parentCommand.getUrl());
        AtomicInteger returnCode = new AtomicInteger(0);
        ExceptionListener exceptionListener = e -> {
            returnCode.compareAndSet(0, 1);
            System.out.println("Exception received: " + e);
            counter.countDown();
        };
        try (Sink sink = new Sink(
                parentCommand.getUrl(),
                queueName,
                parentCommand.createContext(),
                new Sink.DefaultMessageListener(),
                exceptionListener)
        ) {
            sink.start();
            counter.await();
        }
        return 0;
    }

}
