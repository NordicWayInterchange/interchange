package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.peers;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.ListPeerPrivateChannels;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(name = "get", description = "Get all private channels with service provider as peer",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """
                        Example:\n
                        serviceproviderclient privatechannels peers get
                        """
        })
public class GetPeerPrivateChannels implements Callable<Integer> {

    @ParentCommand
    PeersCommand parentCommand;

    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().getParent().createClient();
        ObjectMapper mapper = new ObjectMapper();
        ListPeerPrivateChannels result = client.getPeerPrivateChannels();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        return 0;
    }
}