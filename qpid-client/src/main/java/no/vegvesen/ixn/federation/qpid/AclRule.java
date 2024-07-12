package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;
import java.util.Objects;

//TODO attributes as a separate object, ignoring all unknown attributes??
@JsonIgnoreProperties(ignoreUnknown = true)
public class AclRule {

    private String identity;

    private String operation;

    private String outcome;

    private String objectType;

    private Map<String,String> attributes;


    public AclRule() {

    }

    public AclRule(String identity, String operation, String outcome, String objectType, Map<String,String> attributes) {
        this.identity = identity;
        this.operation = operation;
        this.outcome = outcome;
        this.objectType = objectType;
        this.attributes = attributes;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "NewAclRule{" +
                "identity='" + identity + '\'' +
                ", operation='" + operation + '\'' +
                ", outcome='" + outcome + '\'' +
                ", objectType='" + objectType + '\'' +
                ", attributes=" + attributes +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AclRule that = (AclRule) o;
        return Objects.equals(identity, that.identity) && Objects.equals(operation, that.operation) && Objects.equals(outcome, that.outcome) && Objects.equals(objectType, that.objectType) && Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identity, operation, outcome, objectType, attributes);
    }
}
