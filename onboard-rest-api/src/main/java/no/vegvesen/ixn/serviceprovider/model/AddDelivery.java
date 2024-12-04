package no.vegvesen.ixn.serviceprovider.model;

public class AddDelivery {

    private String selector;

    private String description;

    public AddDelivery() {

    }

    public AddDelivery(String selector){
        this.selector = selector;
    }

     public AddDelivery(String selector, String description) {
        this.selector = selector;
        this.description = description;
     }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getSelector() {
        return selector;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "AddDelivery{" +
                "selector='" + selector + '\'' +
                "description=" + description +
                '}';
    }
}


