package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateExchangeRequest {


    private String name;

    private String type;


    public CreateExchangeRequest() {

    }

    public CreateExchangeRequest(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }


    public String getType() {
        return type;
    }


    @Override
    public String toString() {
        return "CreateExchangeRequest{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
