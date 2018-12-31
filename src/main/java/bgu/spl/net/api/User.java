package bgu.spl.net.api;

import java.util.LinkedList;

public class User {

    // fields

    private String username, password;
    private LinkedList<User> followers, following;

    public User(String username, String password) {

        this.username = username;
        this.password = password;
        followers = new LinkedList<>();
        following = new LinkedList<>();
    }
}