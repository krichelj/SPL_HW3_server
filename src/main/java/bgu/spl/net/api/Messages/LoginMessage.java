package bgu.spl.net.api.Messages;

import bgu.spl.net.api.BGSMessage;
import bgu.spl.net.api.User;

public class LoginMessage extends BGSMessage {

    // fields

    private User userToLogin;

    // constructor

    public LoginMessage(String username, String password) {

        super((short) 2);
        userToLogin = new User (username,password);
    }

    public User getUserToLogin() {

        return userToLogin;
    }

    // methods

    /*@Override
    public BGSMessage execute() {

        if (usersInstance.isUserLoggedIn(userToLogin))
            outputMessage = new ErrorMessage(getOpCode());
        else {
            usersInstance.logUserIn(userToLogin);
            outputMessage = new AckMessage(getOpCode());
        }

        return outputMessage;
    }*/
}
