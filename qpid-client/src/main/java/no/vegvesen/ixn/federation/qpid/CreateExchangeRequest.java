package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateExchangeRequest {


    private String name;

    private String type;

    private AlternateBinding alternateBinding;

    public CreateExchangeRequest() {

    }

    public CreateExchangeRequest(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public CreateExchangeRequest(String name, String type, AlternateBinding alternateBinding) {
        this.name = name;
        this.type = type;
        this.alternateBinding = alternateBinding;
    }

    public String getName() {
        return name;
    }


    public String getType() {
        return type;
    }

    public AlternateBinding getAlternateBinding() {
        return alternateBinding;
    }

    @Override
    public String toString() {
        return "CreateExchangeRequest{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", alternateBinding=" + alternateBinding +
                '}';
    }
}
