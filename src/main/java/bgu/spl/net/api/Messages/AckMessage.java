package bgu.spl.net.api.Messages;

import bgu.spl.net.api.BGSMessage;

import java.io.Serializable;

public class AckMessage extends BGSMessage {

    // fields

    private short messageOpCope;

    // constructor

    public AckMessage(short messageOpCope) {

        super((short) 10);
        this.messageOpCope = messageOpCope;
    }

    // methods

    @Override
    public Serializable execute() {

        return null;
    }

    public short getMessageOpCope() {

        return messageOpCope;
    }
}
