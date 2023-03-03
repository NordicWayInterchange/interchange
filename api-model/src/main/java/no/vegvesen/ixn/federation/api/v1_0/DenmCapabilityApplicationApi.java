package no.vegvesen.ixn.federation.api.v1_0;

import java.util.HashSet;
import java.util.Set;

public class DenmCapabilityApplicationApi extends CapabilityApplicationApi {

    private Set<String> causeCode = new HashSet<>();

    public DenmCapabilityApplicationApi() {

    }

    public DenmCapabilityApplicationApi(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> causeCode) {
        super(Constants.DENM, publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
        if (causeCode != null) {
            this.causeCode.addAll(causeCode);
        }
    }

    public Set<String> getCauseCode() {
        return causeCode;
    }

    public void setCauseCode(Set<String> causeCode) {
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
