package bgu.spl.net.api;

public class BGSSavedMessage {

    // fields

    private final String senderUsername, content;

    // constructor

    protected BGSSavedMessage(String senderUsername, String content) {

        this.senderUsername = senderUsername;
        this.content = content;
    }

    // methods

    public String getSenderUsername() {

        return senderUsername;
    }

    public String getContent() {

        return content;
    }
}
