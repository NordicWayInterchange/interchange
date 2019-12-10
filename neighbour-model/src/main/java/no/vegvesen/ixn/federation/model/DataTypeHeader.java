package no.vegvesen.ixn.federation.model;

public class DataTypeHeader {

    //TODO database annotation magic
    private String headerName;
    private String headerValue;


    public DataTypeHeader(String headerName, String headerValue) {
        this.headerName = headerName;
        this.headerValue = headerValue;
    }

    public String getHeaderValue() {
        return headerValue;
    }

    public String getHeaderName() {
        return headerName;
    }
}
