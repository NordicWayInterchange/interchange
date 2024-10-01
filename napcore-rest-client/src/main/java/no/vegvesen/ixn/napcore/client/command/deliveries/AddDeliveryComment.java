package no.vegvesen.ixn.napcore.client.command.deliveries;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.napcore.client.NapRESTClient;
import no.vegvesen.ixn.napcore.model.AddCommentRequest;
import no.vegvesen.ixn.napcore.model.Delivery;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.PropertiesDefaultProvider;

import java.util.concurrent.Callable;

@Command(
        name = "comment",
        description = "Add comment on nap delivery",
        defaultValueProvider = PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true
)
public class AddDeliveryComment implements Callable<Integer> {

    @ParentCommand
    DeliveriesCommand parentCommand;

    @Parameters(index = "0", description = "The ID of the nap delivery")
    String deliveryId;

    @Parameters(index = "1", description = "Comment to add on delivery")
    String comment;

    @Override
    public Integer call() throws Exception {
        NapRESTClient client = parentCommand.getParentCommand().createClient();
        ObjectMapper mapper = new ObjectMapper();

        Delivery response = client.addDeliveryComment(new AddCommentRequest(deliveryId, comment));
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
        return 0;
    }
}
