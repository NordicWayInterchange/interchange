package no.vegvesen.ixn;

import org.apache.qpid.jms.JmsClientProperties;

/**
 * Class that encapsulates the common application properties in NW2, after the C-ROADS harmonisation
 */
public enum  CommonApplicationProperties {

    USER_ID(JmsClientProperties.JMSXUSERID,true),
    //publisherId - Optional
    PUBLISHER_ID("publisherId",false),
    //publisherName - Mandatory
    PUBLISHER_NAME("publisherName",true),
    //originatingCountry  - Mandatory
    ORIGINATING_COUNTRY("originatingCountry",true),
    //protocolVersion - Mandatory
    PROTOCOL_VERSION("protocolVersion",true),
    //messageType - Mandatory
    MESSAGE_TYPE("messageType",true),
    //content-type - Optional?
    CONTENT_TYPE("contentType",false),
    //latitude - Mandatory
    LATITUDE("latitude",true),
    //longitude - Mandatory
    LONGITUDE("longitude",true),
    //quadTree - Optional, but should be generated if not existing?
    QUAD_TREE("quadTree",false),
    //timestamp - Optional
    TIMESTAMP("timestamp",false),
    //relation - Optional??
    RELATION("relation",false);


    private String propertyName;
    private boolean mandatory;


    CommonApplicationProperties(String propertyName, boolean mandatory) {
        this.propertyName = propertyName;
        this.mandatory = mandatory;
    }

    public String getPropertyName() {
        return propertyName;
    }


}
