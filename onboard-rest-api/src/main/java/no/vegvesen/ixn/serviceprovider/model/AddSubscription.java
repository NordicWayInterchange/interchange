package no.vegvesen.ixn.serviceprovider.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

public class AddSubscription {


    String selector;

    public AddSubscription(){}

    public AddSubscription(String selector){
        this.selector = selector;
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
        return selector.equals(that.selector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selector);
    }

    @Override
    public String toString() {
        return "AddSubscription{" +
                ", selector=" + selector +
                '}';
    }
}