package bgu.spl.net.api.Messages;

import bgu.spl.net.api.BGSMessage;

import java.io.Serializable;

public class StatsMessage extends BGSMessage {

    // fields

    private String username;

    // constructor

    public StatsMessage(String username) {

        super((short) 8);
    }

    // methods

    @Override
    public Serializable execute() {

        return null;
    }
}
