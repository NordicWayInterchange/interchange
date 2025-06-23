package no.vegvesen.ixn.federation.adminserver.qpid;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateQueueRequest {

    private String name;

    private Long maximumMessageTtl;

    private Boolean ensureNondestructiveConsumers;

    public CreateQueueRequest() {
    }

   public CreateQueueRequest(String name, Long maximumMessageTtl) {
       this.name = name;
       this.maximumMessageTtl = maximumMessageTtl;
   }

    public CreateQueueRequest(String name, Long maximumMessageTtl, Boolean ensureNondestructiveConsumers) {
        this.name = name;
        this.maximumMessageTtl = maximumMessageTtl;
        this.ensureNondestructiveConsumers = ensureNondestructiveConsumers;
    }

   public CreateQueueRequest(String name) {
        this(name,null);
   }


    public String getName() {
        return name;
    }

    public Long getMaximumMessageTtl() {
        return maximumMessageTtl;
    }

    public Boolean getEnsureNondestructiveConsumers() {
        return ensureNondestructiveConsumers;
    }

    @Override
    public String toString() {
        return "CreateQueueRequest{" +
                "name='" + name + '\'' +
                ", maximumMessageTtl=" + maximumMessageTtl +
                ", ensureNondestructiveConsumers=" + ensureNondestructiveConsumers +
                '}';
    }
}
