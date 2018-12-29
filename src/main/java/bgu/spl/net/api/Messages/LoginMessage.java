package bgu.spl.net.api.Messages;

import bgu.spl.net.api.BGSMessage;

import java.io.Serializable;

public class LoginMessage extends BGSMessage {

    // fields

    private String username, password;

    // constructor

    public LoginMessage(String username, String password) {

        super((short) 2);
        this.username = username;
        this.password = password;
    }

    // methods

    @Override
    public Serializable execute() {

        return null;
    }
}
