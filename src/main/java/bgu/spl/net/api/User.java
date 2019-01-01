package bgu.spl.net.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class User {

    // fields

    private String username, password;
    private ConcurrentHashMap<String,User> followers, following;
    private LinkedList<String> posts;

    public User(String username, String password) {

        this.username = username;
        this.password = password;
        followers = new ConcurrentHashMap<>();
        following = new ConcurrentHashMap<>();
    }

    public String getUsername() {

        return username;
    }

    public String getPassword() {

        return password;
    }

    public HashMap<String,User> addToFollowing (Collection<User> users){

        HashMap<String,User> addedUsersMap = new HashMap<>();

        for (User currentUser : users)
            if (!following.contains(currentUser)) { // checks if this user is not following the currentUser
                following.put(currentUser.getUsername(), currentUser);
                addedUsersMap.put(currentUser.getUsername(), currentUser);
            }

        return addedUsersMap;
    }

    public HashMap<String,User> removeFromFollowing (Collection<User> users) {

        HashMap<String,User> removedUsersMap = new HashMap<>();

        for (User currentUser : users)
            if (following.contains(currentUser)) { // checks if this user is following the currentUser
                following.remove(currentUser.getUsername());
                removedUsersMap.put(currentUser.getUsername(), currentUser);
            }

        return removedUsersMap;
    }
}