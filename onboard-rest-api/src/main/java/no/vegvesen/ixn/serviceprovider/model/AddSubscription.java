package no.vegvesen.ixn.serviceprovider.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

public class AddSubscription {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String consumerCommonName;

    SelectorApi selector;

    public AddSubscription(){}

    public AddSubscription(String consumerCommonName, SelectorApi selector){
        this.consumerCommonName = consumerCommonName;
        this.selector = selector;
    }

    public String getConsumerCommonName() {
        return consumerCommonName;
    }

    public void setConsumerCommonName(String consumerCommonName) {
        this.consumerCommonName = consumerCommonName;
    }

    public SelectorApi getSelector() {
        return selector;
    }

    public void setSelector(SelectorApi selector) {
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