package no.vegvesen.ixn.serviceprovider.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

public class SelectorApi {
    private String selector;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean createNewQueue;

    public SelectorApi() {

    }

    public SelectorApi(String selector, Boolean createNewQueue){
        this.selector = selector;
        this.createNewQueue = createNewQueue;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getSelector() {
        return selector;
    }

    public void setCreateNewQueue(boolean createNewQueue) {
        this.createNewQueue = createNewQueue;
    }

    public boolean isCreateNewQueue() {
        return (createNewQueue != null) && createNewQueue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SelectorApi)) return false;
        SelectorApi that = (SelectorApi) o;
        return createNewQueue == that.createNewQueue &&
                selector.equals(that.selector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selector, createNewQueue);
    }

    @Override
    public String toString() {
        return "SelectorApi{" +
                "selector='" + selector + '\'' +
                "createNewQueue='" + createNewQueue + '\'' +
                '}';
    }
}


