package no.vegvesen.ixn.serviceprovider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OldLocalActorSubscription {
    private String selector;
    private String queueConsumerUser;


    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getQueueConsumerUser() {
        return queueConsumerUser;
    }

    public void setQueueConsumerUser(String queueConsumerUser) {
        this.queueConsumerUser = queueConsumerUser;
    }
}
