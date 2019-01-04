package bgu.spl.net.api.BGSMessages;

import bgu.spl.net.api.BGSMessage;
import bgu.spl.net.api.User;

public class RegisterMessage extends BGSMessage {

    // fields

    private User userToRegister;

    // constructor

    public RegisterMessage(String username, String password) {

        super((short) 1);
        userToRegister = new User (username,password);
    }

    // methods

    public User getUserToRegister() {

        return userToRegister;
    }
}