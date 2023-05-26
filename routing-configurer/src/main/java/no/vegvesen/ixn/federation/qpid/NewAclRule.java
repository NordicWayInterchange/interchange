package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NewAclRule {

    private String identity;

    //TODO make an enum
    private String operation;

    //TODO make an enum
    private String outcome;

    //TODO enum
    private String objectType;

    private Map<String,String> attributes;
}
