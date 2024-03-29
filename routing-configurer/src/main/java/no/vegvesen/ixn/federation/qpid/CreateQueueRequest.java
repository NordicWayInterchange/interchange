package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateQueueRequest {


    private String name;

    private Long maximumMessageTtl;

    public CreateQueueRequest() {
    }

   public CreateQueueRequest(String name, Long maximumMessageTtl) {
       this.name = name;
       this.maximumMessageTtl = maximumMessageTtl;
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
}
