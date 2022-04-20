package no.vegvesen.ixn;

import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.properties.MessageProperty;

import java.util.*;

public class HeaderBuilder {
    private Map<String,String> headers = new HashMap<>();

    public HeaderBuilder denmMessage() {
        headers.put(MessageProperty.MESSAGE_TYPE.getName(), Constants.DENM);
        return this;
    }

    public HeaderBuilder camMessage() {
        headers.put(MessageProperty.MESSAGE_TYPE.getName(), Constants.CAM);
        return this;
    }

    public HeaderBuilder protocoVersion(String protocolVersion) {
        headers.put(MessageProperty.PROTOCOL_VERSION.getName(), protocolVersion);
        return this;
    }

    public HeaderBuilder originatingCountry(String originatingCountry) {
        headers.put(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
        return this;
    }

    public HeaderBuilder publisherId(String publisherId) {
        headers.put(MessageProperty.PUBLISHER_ID.getName(), publisherId);
        return this;
    }

    public HeaderBuilder quadTree(String ... quadTrees) {
        String formatted = formatQuadTree(quadTrees);
        headers.put(MessageProperty.QUAD_TREE.getName(), formatted);
        return this;
    }

    public HeaderBuilder causeCode(String cause) {
        headers.put(MessageProperty.CAUSE_CODE.getName(), cause);
        return this;
    }

    public HeaderBuilder subCauseCode(String subCause) {
        headers.put(MessageProperty.SUB_CAUSE_CODE.getName(), subCause);
        return this;
    }


    public static String formatQuadTree(String... quadTrees) {
        if (quadTrees.length < 1) {
            throw new IllegalArgumentException("At least one quadTree must be supplied");
        }
        return String.format(",%s,", String.join(",", quadTrees));
    }

    //TODO should probably return something else here
    public Map<String,String> build() {
        //TODO need to know what the message type is...
        String messageTypeName = MessageProperty.MESSAGE_TYPE.getName();
        String messageType =  headers.get(messageTypeName);
        if (messageType == null) {
            throw new HeaderMissingException(Collections.singleton(messageTypeName));
        }
        switch (messageType) {
            case Constants.DENM:
                checkHeadersExist(MessageProperty.mandatoryDenmPropertyNames);
                break;
            case Constants.DATEX_2:
                checkHeadersExist(MessageProperty.mandatoryDatex2PropertyNames);
                break;
            case Constants.CAM:
                checkHeadersExist(MessageProperty.mandatoryCamPropertyNames);
                break;
            case Constants.IVIM:
                checkHeadersExist(MessageProperty.mandatoryIvimPropertyNames);
                break;
            case Constants.MAPEM: //NOTE fallthrough, as they have the same headers
            case Constants.SPATEM:
                checkHeadersExist(MessageProperty.mandatorySpatemMapemPropertyNames);
                break;
            case Constants.SREM: //NOTE fallthrough, as they have the same headers.
            case Constants.SSEM:
                checkHeadersExist(MessageProperty.mandatorySremSsemPropertyNames);
                break;
        }
        return headers;
    }

    private void checkHeadersExist(Set<String> propertyNames) {
        if (! headers.keySet().containsAll(propertyNames)) {
            Set<String> missingHeaders = new HashSet<>(propertyNames);
            missingHeaders.removeAll(headers.keySet());
            throw new HeaderMissingException(missingHeaders);
        }
    }
}
