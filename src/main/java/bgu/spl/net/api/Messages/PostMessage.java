package bgu.spl.net.api.Messages;

import bgu.spl.net.api.BGSMessage;

import java.io.Serializable;

public class PostMessage extends BGSMessage {

    // fields

    private String content;

    // constructor

    public PostMessage(String content) {

        super((short) 5);
    }

    public String getContent() {

        return content;
    }
}
