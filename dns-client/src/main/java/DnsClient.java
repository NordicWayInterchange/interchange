/*-
 * #%L
 * dns-client
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
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
