package bgu.spl.net.api.Messages;

import bgu.spl.net.api.BGSMessage;
import bgu.spl.net.api.User;

import java.util.concurrent.ConcurrentHashMap;

public class AckMessage extends BGSMessage {

    // fields

    private short messageOpCope;
    private int numOfUsers;
    ConcurrentHashMap<String, User> userNameMap;

    // constructor

    public AckMessage(short messageOpCope) {

        super((short) 10);
        this.messageOpCope = messageOpCope;
    }

    // methods

    public short getMessageOpCope() {

        return messageOpCope;
    }

    public void setNumOfUsers(int numOfUsers) {

        this.numOfUsers = numOfUsers;
    }
}
