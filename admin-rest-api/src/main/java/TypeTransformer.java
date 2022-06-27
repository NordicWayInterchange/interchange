
import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.serviceprovider.model.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class TypeTransformer {


    public static List<String> makeHostAndPortOfUrl(String url){
        URI uri = URI.create(url);
        String host = uri.getHost();
        int port = uri.getPort();
        if (port == -1){
            port = 5671;
        }
        return new ArrayList<>(Arrays.asList(host, String.valueOf(port)));
    }
}
