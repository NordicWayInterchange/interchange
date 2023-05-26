package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VirtualHostAccessControlProvider {

    private String id;

    private List<NewAclRule> rules;

    public VirtualHostAccessControlProvider() {
    }

    public VirtualHostAccessControlProvider(String id, List<NewAclRule> rules) {
        this.id = id;
        this.rules = rules;
    }

    public String getId() {
        return id;
    }

    public List<NewAclRule> getRules() {
        return rules;
    }
}
