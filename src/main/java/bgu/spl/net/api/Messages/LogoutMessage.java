package bgu.spl.net.api.Messages;

import bgu.spl.net.api.BGSMessage;


public class LogoutMessage extends BGSMessage {

    // constructor

    public LogoutMessage() {

        super((short) 3);
    }

    // methods

    /*@Override
    public BGSMessage execute() {

        return new LogoutMessage();
    }*/
}
