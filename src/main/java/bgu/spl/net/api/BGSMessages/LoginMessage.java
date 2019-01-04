package bgu.spl.net.api.BGSMessages;

import bgu.spl.net.api.BGSMessage;
import bgu.spl.net.api.User;

public class LoginMessage extends BGSMessage {

    // fields

    private final User userToLogin;

    // constructor

    public LoginMessage(String username, String password) {

        super((short) 2);
        userToLogin = new User (username,password);
    }

    // methods

    public User getUserToLogin() {

        return userToLogin;
    }
}

