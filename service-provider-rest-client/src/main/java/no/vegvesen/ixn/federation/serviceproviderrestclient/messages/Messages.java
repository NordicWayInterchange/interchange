package no.vegvesen.ixn.federation.serviceproviderrestclient.messages;

import java.util.List;

public class Messages {

    List<? extends Message> messages;

    public Messages() {

    }

    public Messages(List<? extends Message> messages) {
        this.messages = messages;
    }

    public List<? extends Message> getMessages() {
        return messages;
    }

}
