package bgu.spl.net.api.Messages;

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

    /*@Override
    public BGSMessage execute() {

        if (usersInstance.isUserRegistered(userToRegister))
            outputMessage = new ErrorMessage(getOpCode());
        else {
            usersInstance.registerUser(userToRegister);
            outputMessage = new AckMessage(getOpCode());
        }

        return outputMessage;
    }*/
}