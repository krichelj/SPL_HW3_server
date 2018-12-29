package bgu.spl.net.api.Messages;

import bgu.spl.net.api.BGSMessage;

import java.io.Serializable;

public class LogoutMessage extends BGSMessage {

    // constructor

    public LogoutMessage() {

        super((short) 3);
    }

    // methods

    @Override
    public Serializable execute() {

        return null;
    }
}
