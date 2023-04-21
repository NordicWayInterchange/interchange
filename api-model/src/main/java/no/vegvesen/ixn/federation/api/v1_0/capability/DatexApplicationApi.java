package no.vegvesen.ixn.federation.api.v1_0.capability;

import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;

import java.util.Set;

public class DatexApplicationApi extends ApplicationApi {

    private String publicationType;

    public DatexApplicationApi() {

    }

    public DatexApplicationApi(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree, String publicationType) {
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
