package no.vegvesen.ixn.serviceprovider.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

public class AddSubscription {


    String selector;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String consumerCommonName;

    String description;

    public AddSubscription(){}

    public AddSubscription(String selector, String description){
        this.selector = selector;
        this.description = description;
    }

    public AddSubscription(String selector, String consumerCommonName, String description) {
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
        this.description = description;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getConsumerCommonName() {
        return consumerCommonName;
    }

    public void setConsumerCommonName(String consumerCommonName) {
        this.consumerCommonName = consumerCommonName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AddSubscription)) return false;
        AddSubscription that = (AddSubscription) o;
        return selector.equals(that.selector) &&
                Objects.equals(consumerCommonName, that.consumerCommonName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selector, consumerCommonName);
    }

    @Override
    public String toString() {
        return "AddSubscription{" +
                "selector=" + selector +
                ", consumerCommonName=" + consumerCommonName +
                ", description=" + description +
                '}';
    }
}