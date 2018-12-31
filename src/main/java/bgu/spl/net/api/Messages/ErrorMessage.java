package bgu.spl.net.api.Messages;

import bgu.spl.net.api.BGSMessage;

import java.io.Serializable;

public class ErrorMessage extends BGSMessage {

    // fields

    private short messageOpCope;

    // constructor

    public ErrorMessage(short messageOpCope) {

        super((short) 11);
        this.messageOpCope = messageOpCope;
    }

    // methods

    /*@Override
    public BGSMessage execute() {

        return null;
    }*/

    public short getMessageOpCope() {

        return messageOpCope;
    }
}
