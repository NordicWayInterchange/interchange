import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.discoverer.DNSProperties;
import no.vegvesen.ixn.federation.model.Neighbour;

import java.util.List;

public class DnsClient {

    public static void main(String[] args) {
        String domainName = "pilotinterchange.eu";
        if (args.length > 0) {
           domainName = args[0];
        }

        DNSProperties properties = new DNSProperties(domainName);
        DNSFacade facade = new DNSFacade(properties);

        System.out.println(facade.getDnsServerName());
        List<Neighbour> neighbours = facade.lookupNeighbours();
        for (Neighbour neighbour : neighbours) {
            System.out.println(neighbour.getName());
        }

    }
}
