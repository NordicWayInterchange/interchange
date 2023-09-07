package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class CreateQueueRequest {


    @JsonUnwrapped
    private Queue queue;

    public CreateQueueRequest() {
    }

   public CreateQueueRequest(Queue queue) {
       this.queue = queue;
   }



}
