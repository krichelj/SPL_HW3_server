package bgu.spl.net.api;

public abstract class BGSSavedMessage {

    // fields

    private final String senderUsername, content;
    protected char messageType;

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

    public char getMessageType(){

        return messageType;
    }
}
