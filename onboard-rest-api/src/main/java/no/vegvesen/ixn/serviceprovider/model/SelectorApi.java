package no.vegvesen.ixn.serviceprovider.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

public class SelectorApi {
    private String selector;

    public SelectorApi() {

    }

    public SelectorApi(String selector) {
        this.selector = selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getSelector() {
        return selector;
    }

    @Override
    public String toString() {
        return "SelectorApi{" +
                "selector='" + selector + '\'' +
                '}';
    }
}


