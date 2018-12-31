package bgu.spl.net.api;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class BGSUsers {

    private final LinkedList<User> registeredUsers, loggedInUsers;
    private final ConcurrentHashMap<User, Pair<LinkedList<User>, LinkedList<User>>> registeredUsersAndTheirFollowers;

    // constructor

    public BGSUsers() {

        registeredUsers = new LinkedList<>();
        loggedInUsers = new LinkedList<>();
        registeredUsersAndTheirFollowers = new ConcurrentHashMap<>();
    }

    // methods

    public void registerUser (User user){

        registeredUsers.add(user);
    }

    public void logUserIn (User user){

        loggedInUsers.add(user);
    }

    public boolean isUserRegistered (User user){

        return registeredUsers.contains(user);
    }

    public boolean isUserLoggedIn (User user){

        return loggedInUsers.contains(user);
    }

    public void logUserOut (User user){

        loggedInUsers.remove(user);
    }
}
