import no.vegvesen.ixn.federation.model.Neighbour;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.apache.http.impl.client.HttpClients;


import javax.net.ssl.SSLContext;
import java.util.List;

public class AdminRESTClient {

    private RestTemplate restTemplate;
    private final String server;
    private final String user;

    public AdminRESTClient(SSLContext sslContext, String server, String user) {
        this.restTemplate = new RestTemplate(
                new HttpComponentsClientHttpRequestFactory(
                        HttpClients
                                .custom()
                                .setSSLContext(sslContext)
                                .build()
                )
        );
        this.server = server;
        this.user = user;
    }

    //TODO: Make response objects!

    public List<Neighbour> getAllNeighbours() {
        return null;
    }
}
