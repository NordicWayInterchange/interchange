package no.vegvesen.ixn.federation.api.v1_0.capability;

import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;

import java.util.HashSet;
import java.util.Set;

public class DenmApplicationApi extends ApplicationApi {

    private Set<Integer> causeCodes = new HashSet<>();

    public DenmApplicationApi() {

    }

    public DenmApplicationApi(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<Integer> causeCodes) {
        super(Constants.DENM, publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
        if (causeCodes != null) {
            this.causeCodes.addAll(causeCodes);
        }
    }

    public Set<Integer> getCauseCodes() {
        return causeCodes;
    }

    public void setCauseCode(Set<Integer> causeCodes) {
        this.causeCodes.clear();
        if (causeCodes != null){
            this.causeCodes.addAll(causeCodes);
        }
    }

    @Override
    public String toString() {
        return "DenmCapabilityApplicationApi{" +
                "causeCode=" + causeCodes +
                '}' + super.toString();
    }
}
