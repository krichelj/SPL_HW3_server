package bgu.spl.net.api.Messages;

import bgu.spl.net.api.BGSMessage;

import java.io.Serializable;

public class PMMessage extends BGSMessage {

    // fields

    private String username, content;

    // constructor

    public PMMessage(String username, String content) {

        super((short) 6);
        this.username = username;
        this.content = content;
    }

    // methods

    /*@Override
    public BGSMessage execute() {

        return null;
    }*/
}
