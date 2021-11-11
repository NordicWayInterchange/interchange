package no.vegvesen.ixn.serviceprovider.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

public class AddSubscription {

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    boolean createNewQueue = false;

    SelectorApi selector;

    public AddSubscription(){}

    public AddSubscription(boolean createNewQueue, SelectorApi selector){
        this.createNewQueue = createNewQueue;
        this.selector = selector;
    }

    public boolean isCreateNewQueue() {
        return createNewQueue;
    }

    public void setCreateNewQueue(boolean createNewQueue) {
        this.createNewQueue = createNewQueue;
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
        return createNewQueue == that.createNewQueue &&
                selector.equals(that.selector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createNewQueue, selector);
    }

    @Override
    public String toString() {
        return "AddSubscription{" +
                "createNewQueue=" + createNewQueue +
                ", selector=" + selector +
                '}';
    }
}
