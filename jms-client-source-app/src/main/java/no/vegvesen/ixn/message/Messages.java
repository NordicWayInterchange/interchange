package no.vegvesen.ixn.message;

import java.util.Set;

public class Messages {

    Set<? extends Message> messages;

    public Messages() {

    }

    public Messages(Set<? extends Message> messages) {
        this.messages = messages;
    }

    public Set<? extends Message> getMessages() {
        return messages;
    }

    public void setMessages(Set<? extends Message> messages) {
        this.messages = messages;
    }
}
