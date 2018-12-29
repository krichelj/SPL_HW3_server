package bgu.spl.net.api.Messages;

import bgu.spl.net.api.BGSMessage;

import java.io.Serializable;

public class UserListMessage extends BGSMessage {

    // constructor

    public UserListMessage() {

        super((short) 7);
    }

    // methods

    @Override
    public Serializable execute() {

        return null;
    }
}
