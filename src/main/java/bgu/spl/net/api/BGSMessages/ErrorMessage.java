package bgu.spl.net.api.BGSMessages;

import bgu.spl.net.api.BGSMessage;

public class ErrorMessage extends BGSMessage {

    // fields

    private final short messageOpCope;

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
