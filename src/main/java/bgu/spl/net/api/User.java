package bgu.spl.net.api;

import java.util.LinkedList;

public class User {

    // fields

    private final String username, password;
    private int loggedInConnectionID;

    public User(String username, String password) {

        this.username = username;
        this.password = password;
    }

    public String getUsername() {

        return username;
    }

    public String getPassword() {

        return password;
    }
}