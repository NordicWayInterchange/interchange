package no.vegvesen.ixn.message;

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
