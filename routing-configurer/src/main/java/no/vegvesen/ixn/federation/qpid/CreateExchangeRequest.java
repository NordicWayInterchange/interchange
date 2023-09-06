package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class CreateExchangeRequest {


    @JsonUnwrapped
    private Exchange exchange;

    public CreateExchangeRequest() {

    }

    public CreateExchangeRequest(Exchange exchange) {
        this.exchange = exchange;
    }

    public String getName() {
        return exchange.getName();
    }

    public boolean isDurable() {
        return exchange.isDurable();
    }

    public String getType() {
        return exchange.getType();
    }

}
