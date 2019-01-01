package bgu.spl.net.api;

import bgu.spl.net.api.Messages.*;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.BlockingConnectionHandler;

/**
 * Implements the {@link BidiMessagingProtocol} interface
 */
public class BGSMessagingProtocol implements BidiMessagingProtocol<BGSMessage> {

    // fields

    private int connectionId;
    private ConnectionsImpl<BGSMessage> connections;
    private BlockingConnectionHandler<BGSMessage> currentConnection;
    private final BGSUsers currentServerUsers;
    private boolean shouldTerminate;

    // constructor

    public BGSMessagingProtocol(BGSUsers currentServerUsers) {

        this.currentServerUsers = currentServerUsers;
        shouldTerminate = false;
    }

    // methods

    @Override
    public void start(int connectionId, Connections<BGSMessage> connections) {

        this.connectionId = connectionId;
        this.connections = (ConnectionsImpl<BGSMessage>) connections;
        currentConnection = (BlockingConnectionHandler<BGSMessage>) this.connections.getClient(connectionId);
    }

    @Override
    public void process(BGSMessage message) {

        BGSMessage outputMessage = null; // the output message
        short currentOpCode = message.getOpCode(); // the current op code
        User currentActiveUser = currentConnection.getCurrentActiveUser();

        if (currentOpCode == 1){ // RegisterMessage

            RegisterMessage messageToProcess = (RegisterMessage) message;
            User userToRegister = messageToProcess.getUserToRegister();

            if (currentServerUsers.isUserRegistered(userToRegister)) // checks if the user is already registered
                outputMessage = new ErrorMessage(currentOpCode); // create an Error message for the output
            else {
                currentServerUsers.registerUser(userToRegister); // register the user
                outputMessage = new AckMessage(currentOpCode); // create an Ack message for the output
            }
        }

        else if (currentOpCode == 2){ // LoginMessage

            LoginMessage messageToProcess = (LoginMessage) message;
            User userToLogin = messageToProcess.getUserToLogin();

            // checks if the user is not registered or already logged in or typed in a wrong password
            if (!currentServerUsers.isUserRegistered(userToLogin) ||
                    currentServerUsers.isUserLoggedIn(userToLogin) || currentServerUsers.checkPassword(userToLogin))
                outputMessage = new ErrorMessage(currentOpCode);
            else {
                currentServerUsers.logUserIn(userToLogin); // logs the user in
                currentConnection.setCurrentActiveUser(userToLogin); // updates the connection's current active user
                outputMessage = new AckMessage(currentOpCode);
            }
        }

        else if (currentOpCode == 3){ // LogoutMessage

            if (!currentServerUsers.isUserLoggedIn(currentActiveUser)) {
                outputMessage = new ErrorMessage(currentOpCode);
            }
            else {
                currentServerUsers.logUserOut(currentActiveUser); // logs the user out in case he's logged in
                connections.disconnect(connectionId); // disconnects the current connection
                outputMessage = new AckMessage(currentOpCode);
            }
        }

        else if (currentOpCode == 4){ // FollowUnfollowMessage

            Boolean done = false;
            int numOfDoneUsers = 0;
            FollowUnfollowMessage messageToProcess = (FollowUnfollowMessage) message;

            if (currentServerUsers.isUserLoggedIn(currentActiveUser)) {

                if (messageToProcess.getFollowOrUnfollow() == '0') { // in case the user wants to follow

                    numOfDoneUsers = currentActiveUser.addToFollowing(messageToProcess.getUsers()).size();

                } else {

                    numOfDoneUsers = currentActiveUser.removeFromFollowing(messageToProcess.getUsers()).size();


                }

                if (numOfDoneUsers > 0)
                    done = true;
            }
        }

        else if (currentOpCode == 5){ // PostMessage

            PostMessage messageToProcess = (PostMessage) message;





        }

        else if (currentOpCode == 6){ // FollowUnfollowMessage

            FollowUnfollowMessage messageToProcess = (FollowUnfollowMessage) message;
            /*User userToLogin = messageToProcess.getUserToLogin();

            if (usersInstance.isUserLoggedIn(userToLogin))
                outputMessage = new ErrorMessage(currentOpCode);
            else {
                usersInstance.logUserIn(userToLogin);
                outputMessage = new AckMessage(currentOpCode);
            }*/
        }

        else if (currentOpCode == 7){ // FollowUnfollowMessage

            FollowUnfollowMessage messageToProcess = (FollowUnfollowMessage) message;
            /*User userToLogin = messageToProcess.getUserToLogin();

            if (usersInstance.isUserLoggedIn(userToLogin))
                outputMessage = new ErrorMessage(currentOpCode);
            else {
                usersInstance.logUserIn(userToLogin);
                outputMessage = new AckMessage(currentOpCode);
            }*/
        }

        else if (currentOpCode == 8){ // FollowUnfollowMessage

            FollowUnfollowMessage messageToProcess = (FollowUnfollowMessage) message;
            /*User userToLogin = messageToProcess.getUserToLogin();

            if (usersInstance.isUserLoggedIn(userToLogin))
                outputMessage = new ErrorMessage(currentOpCode);
            else {
                usersInstance.logUserIn(userToLogin);
                outputMessage = new AckMessage(currentOpCode);
            }*/
        }

        currentConnection.send(outputMessage);
    }

    @Override
    public boolean shouldTerminate() {

        return shouldTerminate;
    }


}
