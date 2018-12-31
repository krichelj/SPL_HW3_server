package bgu.spl.net.api.Messages;

import bgu.spl.net.api.BGSMessage;

public class AckMessage extends BGSMessage {

    // fields

    private short messageOpCope;

    // constructor

    public AckMessage(short messageOpCope) {

        super((short) 10);
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
