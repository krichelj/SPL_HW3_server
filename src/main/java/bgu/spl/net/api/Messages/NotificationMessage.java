package bgu.spl.net.api.Messages;

import bgu.spl.net.api.BGSMessage;

import java.io.Serializable;

public class NotificationMessage extends BGSMessage {

    // fields

    char messageType;
    String postingUser, content;

    // constructor

    public NotificationMessage(char messageType, String postingUser, String content) {

        super((short) 9);
        this.messageType = messageType;
        this.postingUser = postingUser;
        this.content = content;
    }

    // methods

    @Override
    public Serializable execute() {

        return null;
    }

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
