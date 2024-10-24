package no.vegvesen.ixn.federation.api.v1_0.capability;

import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.properties.CapabilityProperty;

import java.util.*;

public class DenmApplicationApi extends ApplicationApi {

    private List<Integer> causeCode = new ArrayList<>();

    public DenmApplicationApi() {

    }

    public DenmApplicationApi(String publisherId, String publicationId, String originatingCountry, String protocolVersion, List<String> quadTree, List<Integer> causeCode) {
        super(Constants.DENM, publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
        if (causeCode != null) {
            this.causeCode.addAll(causeCode);
        }
    }

    public List<Integer> getCauseCode() {
        return causeCode;
    }

    public void setCauseCode(Set<Integer> causeCode) {
        this.causeCode.clear();
        if (causeCode != null){
            this.causeCode.addAll(causeCode);
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
        putIntegerMultiValue(values, CapabilityProperty.CAUSE_CODE, this.getCauseCode());
        return values;
    }

    static void putIntegerMultiValue(Map<String, Object> values, CapabilityProperty property, List<Integer> multiValue) {
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
                "causeCode=" + causeCode +
                '}' + super.toString();
    }
}
