package bgu.spl.net.api;

import bgu.spl.net.api.BGSMessages.*;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.BlockingConnectionHandler;

import java.util.LinkedList;

/**
 * Implements the {@link BidiMessagingProtocol} interface
 */
public class BGSMessagingProtocol implements BidiMessagingProtocol<BGSMessage> {

    // fields

    private int connectionId;
    private ConnectionsImpl<BGSMessage> currentServerConnections;
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
        currentServerConnections = (ConnectionsImpl<BGSMessage>) connections;
    }

    @Override
    public void process(BGSMessage message) {

        short currentOpCode = message.getOpCode(); // the current op code
        BlockingConnectionHandler<BGSMessage> currentConnection = currentServerConnections.getClient(connectionId);

        if (currentOpCode == 1){ // RegisterMessage

            User userToRegister = ((RegisterMessage) message).getUserToRegister();

            synchronized (currentServerUsers) { // synchronize the database to prevent users with the same name to register concurrently

                // checks if the user is already registered or a password of zero length
                if (currentServerUsers.isUserRegistered(userToRegister) || userToRegister.getPassword().length() == 0)
                    currentServerConnections.send(connectionId, new ErrorMessage(currentOpCode)); // sends an Error message for the output
                else { // the user is not currently registered
                    currentServerUsers.registerUser(userToRegister); // register the user
                    currentServerConnections.send(connectionId, new AckMessage(currentOpCode)); // create an Ack message for the output
                }
            }
        }

        else if (currentOpCode == 2){ // LoginMessage

            User userToLogin = ((LoginMessage) message).getUserToLogin();

            synchronized (currentServerUsers) { // synchronize the database to prevent two users from logging in concurrently with the same username or the same client

                // checks if there is a user logged into the current connection, if the user is already logged in or typed in a wrong password
                if (currentConnection.hasUserLoggedIn() || currentServerUsers.isUserLoggedIn(userToLogin) || !currentServerUsers.isPasswordValid(userToLogin))
                    currentServerConnections.send(connectionId, new ErrorMessage(currentOpCode));
                else {
                    currentServerUsers.logUserIn(userToLogin); // logs the user in
                    currentServerConnections.logUserIn(connectionId, userToLogin); // updates the connection's current active user
                    currentServerConnections.send(connectionId, new AckMessage(currentOpCode));
                }
            }
        }

        else if (currentOpCode == 8){ // StatsMessage

            StatsMessage messageToProcess = (StatsMessage) message;
            String userToGetStatsName = messageToProcess.getUsername();

            if (currentServerUsers.isUserLoggedIn(userToGetStatsName))
                currentServerConnections.send(connectionId, new AckMessage(currentOpCode, currentServerUsers.getNumOfPosts(userToGetStatsName),
                        currentServerUsers.getNumOfFollowers(userToGetStatsName), currentServerUsers.getNumOfFollowing(userToGetStatsName)));
            else
                currentServerConnections.send(connectionId, new ErrorMessage(currentOpCode));
        }

        else if (currentConnection.hasUserLoggedIn()) { // checks if there's a user logged in to the current connection handler

            User currentActiveUser = currentConnection.getCurrentActiveUser();
            String currentActiveUserName = currentActiveUser.getUsername();

            if (currentOpCode == 3){ // LogoutMessage

                if (!currentServerUsers.isUserLoggedIn(currentActiveUser)) { // checks if the user is not logged in
                    currentServerConnections.send(connectionId, new ErrorMessage(currentOpCode));
                }
                else {
                    currentServerUsers.logUserOut(currentActiveUser); // logs the user out
                    if (currentServerConnections.send(connectionId, new AckMessage(currentOpCode)))
                        currentServerConnections.disconnect(connectionId); // disconnects the current connection
                }
            }

            else if (currentOpCode == 4){ // FollowUnfollowMessage

                FollowUnfollowMessage messageToProcess = (FollowUnfollowMessage) message;
                LinkedList<String> usersToActUpon = messageToProcess.getUsers(), usersSuccessfullyActedUpon = new LinkedList<>();

                if (currentServerUsers.isUserLoggedIn(currentActiveUser)) { // check if the user is logged in

                    if (messageToProcess.getFollowOrUnfollow() == '0')  // in case the user wants to follow
                        usersSuccessfullyActedUpon = currentServerUsers.followUsers(currentActiveUser, usersToActUpon);
                    else // in case the user wants to unfollow
                        usersSuccessfullyActedUpon = currentServerUsers.unfollowUsers(currentActiveUser, usersToActUpon);
                }

                if (usersSuccessfullyActedUpon.size() > 0)
                    currentServerConnections.send(connectionId, new AckMessage(currentOpCode, usersSuccessfullyActedUpon.size(), usersSuccessfullyActedUpon));
                else
                    currentServerConnections.send(connectionId, new ErrorMessage(currentOpCode));
            }

            else if (currentOpCode == 5){ // PostMessage

                PostMessage messageToProcess = (PostMessage) message;

                if (currentServerUsers.isUserLoggedIn(currentActiveUser)) {

                    String postContent = messageToProcess.getContent(); // get the post content
                    LinkedList<String> additionalUsersList = messageToProcess.getAdditionalUsersList(); // get the additional users list

                    currentServerUsers.addPostToUserAndHisFollowersList(currentActiveUserName, postContent); // save the post to user and followers
                    if (!additionalUsersList.isEmpty()) // save the post for the additional users if there are any
                        currentServerUsers.addPostFromPosterToAdditionalUsers(currentActiveUserName, postContent, additionalUsersList);
                }
                else
                    currentServerConnections.send(connectionId, new ErrorMessage(currentOpCode));

            }

            else if (currentOpCode == 6){ // PMMessage

                PMMessage messageToProcess = (PMMessage) message;
                String receiverUsername = messageToProcess.getReceiverUsername();

                if (currentServerUsers.isUserLoggedIn(currentActiveUser) && currentServerUsers.isUserRegistered(receiverUsername))
                    currentServerUsers.addPMToSenderAndReceiver(currentActiveUserName,receiverUsername,messageToProcess.getPMcontent());
                else
                    currentServerConnections.send(connectionId, new ErrorMessage(currentOpCode));

            }

            else if (currentOpCode == 7){ // UserListMessage

                UserListMessage messageToProcess = (UserListMessage) message;
                LinkedList<String> currentlyRegisteredUsers = currentServerUsers.getRegisteredUsers();

                if (currentServerUsers.isUserLoggedIn(currentActiveUser))
                    currentServerConnections.send(connectionId, new AckMessage(currentOpCode, currentlyRegisteredUsers.size(), currentlyRegisteredUsers));
                else
                    currentServerConnections.send(connectionId, new ErrorMessage(currentOpCode));

            }
        }
        else // in case the current connection handler does not have a user logged in
            currentServerConnections.send(connectionId, new ErrorMessage(currentOpCode)); // send an error in case there's no client connected
    }

    @Override
    public boolean shouldTerminate() {

        return shouldTerminate;
    }

}