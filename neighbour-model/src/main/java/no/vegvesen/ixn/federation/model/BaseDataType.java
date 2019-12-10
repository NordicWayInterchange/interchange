package no.vegvesen.ixn.federation.model;

import java.util.Arrays;
import java.util.List;

public class BaseDataType {

    private static List<String> allowedPropertyNames = Arrays.asList("publisherId",
            "publisherName",
            "originatingCountry",
            "protocolVersion",
            "messageType",
            "contentType",
            "latitude",
            "longitude",
            "quadTree",
            "timestamp",
            "relation");

    private List<DataTypeHeader> headers;

    public BaseDataType(List<DataTypeHeader> headers) {
        for (DataTypeHeader header : headers) {
            String headerName = header.getHeaderName();
            if (!allowedPropertyNames.contains(headerName)) {
                throw new IllegalArgumentException(String.format("%s is not a legal header name", headerName));
            }
        }
        //TODO copy
        this.headers = headers;
    }

    public List<DataTypeHeader> getHeaders() {
        return headers;
    }
}
