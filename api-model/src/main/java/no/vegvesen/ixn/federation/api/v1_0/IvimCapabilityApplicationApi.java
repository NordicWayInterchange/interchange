package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.Constants;

import java.util.HashSet;
import java.util.Set;

public class IvimCapabilityApplicationApi extends CapabilityApplicationApi {

    private Set<String> iviType = new HashSet<>();

    public IvimCapabilityApplicationApi() {

    }

    public IvimCapabilityApplicationApi(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> iviType) {
        super(Constants.IVIM,publisherId, publicationId, originatingCountry,protocolVersion,quadTree);
        if (iviType != null) {
            this.iviType.addAll(iviType);
        }
    }

    public Set<String> getIviType() {
        return iviType;
    }

    public void setIviType(Set<String> iviType) {
        this.iviType.clear();
        if (this.iviType != null){
            this.iviType.addAll(iviType);
        }
    }

    @Override
    public String toString() {
        return "IvimCapabilityApplicationApi{" +
                "iviType=" + iviType +
                '}' + super.toString();
    }
}
