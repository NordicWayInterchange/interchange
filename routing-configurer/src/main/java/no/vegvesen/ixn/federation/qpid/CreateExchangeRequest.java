package no.vegvesen.ixn.federation.qpid;

public class CreateExchangeRequest {

    private String name;
    private boolean durable = true;

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

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
