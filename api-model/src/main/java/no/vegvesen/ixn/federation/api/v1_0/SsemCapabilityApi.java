package no.vegvesen.ixn.federation.api.v1_0;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SsemCapabilityApi extends CapabilityApi{

    public SsemCapabilityApi() {

    }

    public SsemCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatusApi redirect, Integer shardCount, String infoUrl) {
        super(Constants.SSEM, publisherId, originatingCountry, protocolVersion, redirect, shardCount, infoUrl, quadTree);
    }

    public SsemCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
        this(publisherId, originatingCountry, protocolVersion, quadTree, null, null, null);
    }

    public SsemCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatusApi redirect) {
        this(publisherId, originatingCountry, protocolVersion, quadTree, redirect, null, null);
    }

    @Override
    public String toString() {
        return "SsemCapabilityApi{" +
                '}' + super.toString();
    }
}
