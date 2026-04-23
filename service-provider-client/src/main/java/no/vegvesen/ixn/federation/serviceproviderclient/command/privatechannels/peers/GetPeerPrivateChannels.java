package no.vegvesen.ixn.federation.serviceproviderclient.command.privatechannels.peers;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.serviceproviderrestclient.ServiceProviderClient;
import no.vegvesen.ixn.serviceprovider.model.PeerPrivateChannelApi;
import picocli.CommandLine;

import java.util.concurrent.Callable;


@CommandLine.Command(name = "get", description = "Get private channel peer with service provider as peer",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        mixinStandardHelpOptions = true,
        version = "1.0",
        customSynopsis = {
                """
                        Example:\n
                        serviceproviderclient privatechannels peers get 43f9ffe7-e8ed-4165-a61c-21f6f2e55a57
                        """
        })
public class GetPeerPrivateChannels implements Callable<Integer> {

    @CommandLine.ParentCommand
    PeersCommand parentCommand;

    @CommandLine.Parameters(index = "0", description = "Private channel Id")
    String privateChannelId;


    @Override
    public Integer call() throws Exception {
        ServiceProviderClient client = parentCommand.getParent().getParent().createClient();
        ObjectMapper mapper = new ObjectMapper();
        PeerPrivateChannelApi result = client.getPeerPrivateChannelById(privateChannelId);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        return 0;
    }
}
