package no.vegvesen.ixn.federation.api.v1_0;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SremCapabilityApi extends CapabilityApi {
    private Set<String> ids = new HashSet<>();

    public SremCapabilityApi() {

    }

    public SremCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatusApi redirect, Integer shardCount, String infoUrl, Set<String> ids) {
        super(Constants.SREM, publisherId, originatingCountry, protocolVersion, redirect, shardCount, infoUrl, quadTree);
        if(ids != null) {
            this.ids.addAll(ids);
        }
    }

    public SremCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> ids) {
        this(publisherId, originatingCountry, protocolVersion, quadTree, null, null, null, ids);
    }

    public SremCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatusApi redirect, Set<String> ids) {
        this(publisherId, originatingCountry, protocolVersion, quadTree, redirect, null, null, ids);
    }

    public Set<String> getIds() {
        return ids;
    }

    public void setIds(Collection<String> ids) {
        this.ids.clear();
        if(ids != null) {
            this.ids.addAll(ids);
        }
    }

    @Override
    public String toString() {
        return "SremCapabilityApi{" +
                "ids=" + ids +
                '}' + super.toString();
    }
}
