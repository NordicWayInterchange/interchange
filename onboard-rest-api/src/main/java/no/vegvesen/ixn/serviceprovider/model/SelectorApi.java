package no.vegvesen.ixn.serviceprovider.model;

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


