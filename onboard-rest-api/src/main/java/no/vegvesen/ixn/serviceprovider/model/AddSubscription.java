package no.vegvesen.ixn.serviceprovider.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

public class AddSubscription {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String consumerCommonName;

    String selector;

    public AddSubscription(){}

    public AddSubscription(String selector){
        this.selector = selector;
    }

    public AddSubscription(String consumerCommonName, String selector){
        this.consumerCommonName = consumerCommonName;
        this.selector = selector;
    }

    public String getConsumerCommonName() {
        return consumerCommonName;
    }

    public void setConsumerCommonName(String consumerCommonName) {
        this.consumerCommonName = consumerCommonName;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AddSubscription)) return false;
        AddSubscription that = (AddSubscription) o;
        return consumerCommonName.equals(that.consumerCommonName) &&
                selector.equals(that.selector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consumerCommonName, selector);
    }

    @Override
    public String toString() {
        return "AddSubscription{" +
                "consumerCommonName=" + consumerCommonName +
                ", selector=" + selector +
                '}';
    }
}