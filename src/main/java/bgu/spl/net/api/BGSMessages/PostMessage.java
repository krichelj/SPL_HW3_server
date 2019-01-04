package bgu.spl.net.api.BGSMessages;

import bgu.spl.net.api.BGSMessage;

import java.util.LinkedList;

public class PostMessage extends BGSMessage {

    // fields

    private final String content;
    private final LinkedList<String> additionalUsersList;

    // constructors

    public PostMessage(String content) { // constructor in case there aren't any additional users to post to

        super((short) 5);
        this.content = content;
        additionalUsersList = null;
    }

    public PostMessage(String content, LinkedList<String> additionalUsersList) { // constructor in case there are additional users to post to

        super((short) 5);
        this.content = content;
        this.additionalUsersList = additionalUsersList;
    }

    public String getContent() {

        return content;
    }

    public LinkedList<String> getAdditionalUsersList() {

        return additionalUsersList;
    }
}