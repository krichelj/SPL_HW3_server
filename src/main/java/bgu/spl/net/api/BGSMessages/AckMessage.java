package bgu.spl.net.api.BGSMessages;

import bgu.spl.net.api.BGSMessage;

import java.util.LinkedList;

public class AckMessage extends BGSMessage {

    // fields

    private final short messageOpCope;
    private short numOfUsers, numOfPosts, numOfFollowers, numOfFollowing;
    private LinkedList<String> userNameList;

    // constructors

    public AckMessage(short messageOpCope) { // regular Ack

        super((short) 10);
        this.messageOpCope = messageOpCope;
    }

    public AckMessage(short messageOpCope, short numOfUsers, LinkedList<String> userNameList) { // FollowUnfollowMessage or UserListMessage Ack

        super((short) 10);
        this.messageOpCope = messageOpCope;
        this.numOfUsers = numOfUsers;
        this.userNameList = userNameList;
    }

    public AckMessage(short messageOpCope, short numOfPosts, short numOfFollowers, short numOfFollowing) { // Stats Ack

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

    public short getNumOfUsers() {

        return numOfUsers;
    }

    public LinkedList<String> getUserNameList() {

        return userNameList;
    }

    public short getNumOfPosts() {

        return numOfPosts;
    }

    public short getNumOfFollowers() {

        return numOfFollowers;
    }

    public short getNumOfFollowing() {

        return numOfFollowing;
    }
}
