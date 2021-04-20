package no.vegvesen.ixn;

import java.util.List;

public class MessageListApi {

    List<MessageApi> messages;

    public MessageListApi() {

    }

    public MessageListApi(List<MessageApi> messages){
        this.messages = messages;
    }

    public List<MessageApi> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageApi> messages) {
        this.messages = messages;
    }
}
