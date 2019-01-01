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

        /*BGSMessage resultMessage = ((BGSMessage) message).execute();*/

        BGSMessage outputMessage = null;
        short currentOpCode = message.getOpCode();

        if (currentOpCode == 1){ // RegisterMessage

            RegisterMessage messageToProcess = (RegisterMessage) message;
            User userToRegister = messageToProcess.getUserToRegister();

            if (currentServerUsers.isUserRegistered(userToRegister))
                outputMessage = new ErrorMessage(currentOpCode);
            else {
                currentServerUsers.registerUser(userToRegister);
                outputMessage = new AckMessage(currentOpCode);
            }
        }

        else if (currentOpCode == 2){ // LoginMessage

            LoginMessage messageToProcess = (LoginMessage) message;
            User userToLogin = messageToProcess.getUserToLogin();

            if (currentServerUsers.isUserLoggedIn(userToLogin))
                outputMessage = new ErrorMessage(currentOpCode);
            else {
                currentServerUsers.logUserIn(userToLogin);
                outputMessage = new AckMessage(currentOpCode);
            }
        }

        else if (currentOpCode == 3){ // LogoutMessage

            connections.disconnect(connectionId);
            shouldTerminate = true;
        }

        else if (currentOpCode == 4){ // FollowUnfollowMessage

            FollowUnfollowMessage messageToProcess = (FollowUnfollowMessage) message;

            if (messageToProcess.getFollowOrUnfollow() == '0') { // in case the user wants to follow



            }

        }

        else if (currentOpCode == 5){ // FollowUnfollowMessage

            FollowUnfollowMessage messageToProcess = (FollowUnfollowMessage) message;
            /*User userToLogin = messageToProcess.getUserToLogin();

            if (usersInstance.isUserLoggedIn(userToLogin))
                outputMessage = new ErrorMessage(currentOpCode);
            else {
                usersInstance.logUserIn(userToLogin);
                outputMessage = new AckMessage(currentOpCode);
            }*/
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

        currentConnection.send(outputMessage);    }

    @Override
    public boolean shouldTerminate() {

        return shouldTerminate;
    }
}
