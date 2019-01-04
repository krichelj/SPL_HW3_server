package bgu.spl.net.api.BGSMessages;

import bgu.spl.net.api.BGSMessage;

import java.util.LinkedList;

public class AckMessage extends BGSMessage {

    // fields

    private final short messageOpCope;
    private int numOfUsers, numOfPosts, numOfFollowers, numOfFollowing;
    private LinkedList<String> userNameList;

    // constructors

    public AckMessage(short messageOpCope) { // regular Ack

        super((short) 10);
        this.messageOpCope = messageOpCope;
    }

    public AckMessage(short messageOpCope, int numOfUsers, LinkedList<String> userNameList) { // UserListMessage Ack

        super((short) 10);
        this.messageOpCope = messageOpCope;
        this.numOfUsers = numOfUsers;
        this.userNameList = userNameList;
    }

    public AckMessage(short messageOpCope, int numOfPosts, int numOfFollowers, int numOfFollowing) { // UserListMessage Ack

        super((short) 10);
        this.messageOpCope = messageOpCope;
        this.numOfPosts = numOfPosts;
        this.numOfFollowers = numOfFollowers;
        this.numOfFollowing = numOfFollowing;
    }


    // methods

    public short getMessageOpCope() {

        return messageOpCope;
    }
}
