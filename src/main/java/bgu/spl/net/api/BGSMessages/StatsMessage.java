package bgu.spl.net.api.BGSMessages;

import bgu.spl.net.api.BGSMessage;


public class StatsMessage extends BGSMessage {

    // fields

    private String username;

    // constructor

    public StatsMessage(String username) {

        super((short) 8);
        this.username = username;
    }

    // methods

    public String getUsername() {

        return username;
    }
}
