package bgu.spl.net.api;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class BGSUsers {

    private final ConcurrentHashMap<String,User> registeredUsers, loggedInUsers;

    // constructor

    public BGSUsers() {

        registeredUsers = new ConcurrentHashMap<>();
        loggedInUsers = new ConcurrentHashMap<>();
    }

    // methods

    public void registerUser (User user){

        registeredUsers.put(user.getUsername(),user);
    }

    public void logUserIn (User user){

        loggedInUsers.put(user.getUsername(),user);
    }

    public boolean isUserRegistered (User user){

        return registeredUsers.contains(user);
    }

    public boolean isUserLoggedIn (User user){

        return user!= null && loggedInUsers.contains(user);
    }

    public void logUserOut (User user){

        loggedInUsers.remove(user);
    }

    public boolean checkPassword (User user){

        return registeredUsers.get(user.getUsername()).getPassword().equals(user.getPassword());
    }
}
