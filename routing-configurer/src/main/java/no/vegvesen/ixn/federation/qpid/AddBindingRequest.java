package no.vegvesen.ixn.federation.qpid;


import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class AddBindingRequest {

    boolean replaceExistingArguments = true;

    @JsonUnwrapped
    private Binding binding;

    public AddBindingRequest() {
    }

    public AddBindingRequest(Binding binding) {
        this.binding = binding;
    }

    @Override
    public String toString() {
        return "AddBindingRequest{" +
                "replaceExistingArguments=" + replaceExistingArguments +
                ", binding=" + binding +
                '}';
    }
}
