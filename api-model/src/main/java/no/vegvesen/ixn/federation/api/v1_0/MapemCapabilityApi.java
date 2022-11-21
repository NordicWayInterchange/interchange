package no.vegvesen.ixn.federation.api.v1_0;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MapemCapabilityApi extends CapabilityApi {
    private Set<String> ids = new HashSet<>();

    public MapemCapabilityApi() {

    }

    public MapemCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatusApi redirect, Integer shardCount, String infoUrl) {
        super(Constants.MAPEM, publisherId, originatingCountry, protocolVersion, redirect, shardCount, infoUrl, quadTree);
    }

    public MapemCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
        this(publisherId,originatingCountry,protocolVersion,quadTree,null,null,null);
    }

    public MapemCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatusApi redirect) {
        this(publisherId,originatingCountry,protocolVersion,quadTree,redirect,null,null);
    }

    @Override
    public String toString() {
        return "MapemCapabilityApi{" +
                '}' + super.toString();
    }
}
