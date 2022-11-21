package no.vegvesen.ixn.federation.api.v1_0;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SremCapabilityApi extends CapabilityApi {

    public SremCapabilityApi() {

    }

    public SremCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatusApi redirect, Integer shardCount, String infoUrl) {
        super(Constants.SREM, publisherId, originatingCountry, protocolVersion, redirect, shardCount, infoUrl, quadTree);
    }

    public SremCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
        this(publisherId, originatingCountry, protocolVersion, quadTree, null, null, null);
    }

    public SremCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatusApi redirect) {
        this(publisherId, originatingCountry, protocolVersion, quadTree, redirect, null, null);
    }

    @Override
    public String toString() {
        return "SremCapabilityApi{" +
                '}' + super.toString();
    }
}
