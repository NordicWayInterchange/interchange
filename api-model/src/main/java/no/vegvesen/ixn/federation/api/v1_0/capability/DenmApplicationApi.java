package no.vegvesen.ixn.federation.api.v1_0.capability;

import io.swagger.v3.oas.annotations.media.Schema;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.properties.CapabilityProperty;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Schema(hidden = true)
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
    public Map<String, Object> getCommonProperties(String messageType) {
        Map<String, Object> values = new HashMap<>();
        putValue(values, CapabilityProperty.MESSAGE_TYPE, messageType);
        putValue(values, CapabilityProperty.PUBLISHER_ID, this.getPublisherId());
        putValue(values, CapabilityProperty.PUBLICATION_ID, this.getPublicationId());
        putValue(values, CapabilityProperty.ORIGINATING_COUNTRY, this.getOriginatingCountry());
        putValue(values, CapabilityProperty.PROTOCOL_VERSION, this.getProtocolVersion());
        putMultiValue(values, CapabilityProperty.QUAD_TREE, this.getQuadTree());
        putIntegerMultiValue(values, CapabilityProperty.CAUSE_CODE, this.getCauseCodes());
        return values;
    }

    static void putIntegerMultiValue(Map<String, Object> values, CapabilityProperty property, Set<Integer> multiValue) {
        if (multiValue.isEmpty()) {
            values.put(property.getName(), null);
        } else {
            StringBuilder builder = new StringBuilder();
            for (Integer value : multiValue) {
                builder.append(value.toString()).append(",");
            }
            values.put(property.getName(), builder.toString());
        }
    }

    @Override
    public String toString() {
        return "DenmCapabilityApplicationApi{" +
                "causeCode=" + causeCodes +
                '}' + super.toString();
    }
}
