package no.vegvesen.ixn.federation.qpid;


public class AddBindingRequest {
    String destination;

    String bindingKey;

    boolean replaceExistingArguments = true;

    Filter arguments;

    public AddBindingRequest() {
    }

    public AddBindingRequest(String destination, String bindingKey, String filter) {
        this.destination = destination;
        this.bindingKey = bindingKey;
        this.arguments = new Filter(filter);
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getBindingKey() {
        return bindingKey;
    }

    public void setBindingKey(String bindingKey) {
        this.bindingKey = bindingKey;
    }

    public boolean isReplaceExistingArguments() {
        return replaceExistingArguments;
    }

    public void setReplaceExistingArguments(boolean replaceExistingArguments) {
        this.replaceExistingArguments = replaceExistingArguments;
    }

    public Filter getArguments() {
        return arguments;
    }

    public void setArguments(Filter arguments) {
        this.arguments = arguments;
    }
}
