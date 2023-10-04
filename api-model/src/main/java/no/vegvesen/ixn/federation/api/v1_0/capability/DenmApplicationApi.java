package no.vegvesen.ixn.federation.api.v1_0.capability;

import no.vegvesen.ixn.federation.api.v1_0.Constants;

import java.util.HashSet;
import java.util.Set;

public class DenmApplicationApi extends ApplicationApi {

    private Set<Integer> causeCode = new HashSet<>();

    public DenmApplicationApi() {

    }

    public DenmApplicationApi(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<Integer> causeCode) {
        super(Constants.DENM, publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
        if (causeCode != null) {
            this.causeCode.addAll(causeCode);
        }
    }

    public Set<Integer> getCauseCode() {
        return causeCode;
    }

    public void setCauseCode(Set<Integer> causeCode) {
        this.causeCode.clear();
        if (causeCode != null){
            this.causeCode.addAll(causeCode);
        }
    }

    @Override
    public String toString() {
        return "DenmCapabilityApplicationApi{" +
                "causeCode=" + causeCode +
                '}' + super.toString();
    }
}
