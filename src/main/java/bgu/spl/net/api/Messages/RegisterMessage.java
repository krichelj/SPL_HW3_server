package bgu.spl.net.api.Messages;

import bgu.spl.net.api.BGSMessage;

import java.io.Serializable;

public class RegisterMessage extends BGSMessage {

    // fields

    private String username, password;

    // constructor

    public RegisterMessage(String username, String password) {

        super((short) 1);
        this.username = username;
        this.password = password;
    }

    // methods

    @Override
    public Serializable execute() {

        return null;
    }
}
