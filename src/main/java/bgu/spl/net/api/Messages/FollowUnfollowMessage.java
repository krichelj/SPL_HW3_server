package bgu.spl.net.api.Messages;

import bgu.spl.net.api.BGSMessage;

import java.io.Serializable;
import java.util.LinkedList;

public class FollowUnfollowMessage extends BGSMessage {

    // fields

    char followOrUnfollow;
    short numOfUsers;
    LinkedList<String> userNameList;

    // constructor

    public FollowUnfollowMessage(char followOrUnfollow, short numOfUsers, LinkedList<String> userNameList) {

        super((short) 4);
        this.followOrUnfollow = followOrUnfollow;
        this.numOfUsers = numOfUsers;
        this.userNameList = userNameList;
    }

    // methods

    @Override
    public Serializable execute() {

        return null;
    }
}
