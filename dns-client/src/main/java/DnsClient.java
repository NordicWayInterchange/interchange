import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.discoverer.DNSProperties;
import no.vegvesen.ixn.federation.model.Neighbour;

import java.util.List;

public class DnsClient {

    public static void main(String[] args) {

        DNSProperties properties = new DNSProperties("443","itsinterchange.eu");
        DNSFacade facade = new DNSFacade(properties);

        System.out.println(facade.getDnsServerName());
        List<Neighbour> neighbours = facade.getNeighbours();
        for (Neighbour neighbour : neighbours) {
            System.out.println(neighbour.getName());
        }

    }
}
