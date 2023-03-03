package no.vegvesen.ixn.federation.api.v1_0;

import java.util.Set;

public class DatexCapabilityApplicationApi extends CapabilityApplicationApi {

    private String publicationType;

    public DatexCapabilityApplicationApi() {

    }

    public DatexCapabilityApplicationApi(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree, String publicationType) {
        super(Constants.DATEX_2, publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
        if (publicationType != null) {
            this.publicationType = publicationType;
        }
    }

    public String getPublicationType() {
        return publicationType;
    }

    public void setPublicationType(String publicationType) {
        this.publicationType = publicationType;
    }

    @Override
    public String toString() {
        return "DatexCapabilityApplicationApi{" +
                "publicationType='" + publicationType + '\'' +
                '}' + super.toString();
    }
}
