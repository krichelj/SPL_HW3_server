package bgu.spl.net.api.BGSMessages;

import bgu.spl.net.api.BGSMessage;
import java.util.LinkedList;

public class FollowUnfollowMessage extends BGSMessage {

    // fields

    private final short followOrUnfollow, numOfUsers;
    private final LinkedList<String> userNameList;

    // constructor

    public FollowUnfollowMessage(short followOrUnfollow, short numOfUsers, LinkedList<String> userNameList) {

        super((short) 4);
        this.followOrUnfollow = followOrUnfollow;
        this.numOfUsers = numOfUsers;
        this.userNameList = userNameList;
    }

    // methods

    public short getFollowOrUnfollow (){

        return followOrUnfollow;
    }

    public LinkedList<String> getUsers (){

        return userNameList;
    }

    public short getNumOfUsers() {

        return numOfUsers;
    }
}
