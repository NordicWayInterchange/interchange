package no.vegvesen.ixn.serviceprovider.capability;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SremSPCapabilityApi extends SPCapabilityApi {
    private Set<String> ids = new HashSet<>();

    public SremSPCapabilityApi() {

    }

    public SremSPCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, SPRedirectStatusApi redirect, Integer shardCount, String infoUrl, Set<String> ids) {
        super(Constants.SREM, publisherId, originatingCountry, protocolVersion, redirect, shardCount, infoUrl, quadTree);
        if(ids != null) {
            this.ids.addAll(ids);
        }
    }

    public SremSPCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> ids) {
        this(publisherId, originatingCountry, protocolVersion, quadTree, null, null, null, ids);
    }

    public SremSPCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, SPRedirectStatusApi redirect, Set<String> ids) {
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
