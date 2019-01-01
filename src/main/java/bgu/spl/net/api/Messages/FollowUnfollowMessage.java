package bgu.spl.net.api.Messages;

import bgu.spl.net.api.BGSMessage;
import bgu.spl.net.api.User;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class FollowUnfollowMessage extends BGSMessage {

    // fields

    char followOrUnfollow;
    short numOfUsers;
    ConcurrentHashMap<String, User> userNameMap;

    // constructor

    public FollowUnfollowMessage(char followOrUnfollow, short numOfUsers, LinkedList<String> userNameList) {

        super((short) 4);
        this.followOrUnfollow = followOrUnfollow;
        this.numOfUsers = numOfUsers;
        this.userNameMap = new ConcurrentHashMap<>();
    }

    // methods

    public char getFollowOrUnfollow (){

        return followOrUnfollow;
    }

    public Collection<User> getUsers (){

        return userNameMap.values();
    }
}
