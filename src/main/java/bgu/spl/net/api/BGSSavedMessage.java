package bgu.spl.net.api;

public abstract class BGSSavedMessage {

    // fields

    private final String senderUsername, content;
    private boolean wasRead;

    // constructor

    protected BGSSavedMessage(String senderUsername, String content) {

        this.senderUsername = senderUsername;
        this.content = content;
        wasRead = false;
    }

    // methods

    public String getSenderUsername() {

        return senderUsername;
    }

    public String getContent() {

        return content;
    }
}
