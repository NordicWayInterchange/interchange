package no.vegvesen.ixn.federation.qpid;

public class CreateQueueRequest {
    private String name;
    private boolean durable = true;

    private long maximumMessageTtl;

    public CreateQueueRequest() {
    }

   public CreateQueueRequest(String name, long maximumMessageTtl) {
       this.name = name;
       this.maximumMessageTtl = maximumMessageTtl;
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

    public long getMaximumMessageTtl() {
        return maximumMessageTtl;
    }

    public void setMaximumMessageTtl(long maximumMessageTtl) {
        this.maximumMessageTtl = maximumMessageTtl;
    }
}
