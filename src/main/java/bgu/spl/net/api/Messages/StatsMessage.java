package bgu.spl.net.api.Messages;

import bgu.spl.net.api.BGSMessage;


public class StatsMessage extends BGSMessage {

    // fields

    private String username;

    // constructor

    public StatsMessage(String username) {

        super((short) 8);
    }

    // methods

    /*@Override
    public BGSMessage execute() {

        return null;
    }*/
}
