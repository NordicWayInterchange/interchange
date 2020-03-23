import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.discoverer.DNSProperties;
import no.vegvesen.ixn.federation.model.Neighbour;

import java.util.List;

public class DnsClient {

    public static void main(String[] args) {
        String controlChannelPort = "443";
        String domainName = "itsinterchange.eu";
        if (args.length > 0) {
           domainName = args[0];
        }
        if (args.length > 1 ) {
            controlChannelPort = args[1];
        }
        DNSProperties properties = new DNSProperties(controlChannelPort, domainName);
        DNSFacade facade = new DNSFacade(properties);

        System.out.println(facade.getDnsServerName());
        List<Neighbour> neighbours = facade.getNeighbours();
        for (Neighbour neighbour : neighbours) {
            System.out.println(neighbour.getName());
        }

    }
}
