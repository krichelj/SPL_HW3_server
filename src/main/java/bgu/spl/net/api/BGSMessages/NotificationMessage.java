package bgu.spl.net.api.BGSMessages;

import bgu.spl.net.api.BGSMessage;

public class NotificationMessage extends BGSMessage {

    // fields

    private char messageType;
    private String postingUser, content;

    // constructor

    public NotificationMessage(char messageType, String postingUser, String content) {

        super((short) 9);
        this.messageType = messageType;
        this.postingUser = postingUser;
        this.content = content;
    }

    // methods

    public char getMessageType() {

        return messageType;
    }
    public String getPostingUser() {

        return postingUser;
    }

    public String getContent() {

        return content;
    }
}
