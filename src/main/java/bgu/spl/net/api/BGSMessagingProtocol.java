package bgu.spl.net.api;

import bgu.spl.net.api.BGSMessages.*;
import bgu.spl.net.api.BGSSavedMessages.PM;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Implements the {@link BidiMessagingProtocol} interface
 */
public class BGSMessagingProtocol implements BidiMessagingProtocol<BGSMessage> {

    // fields

    private int connectionId;
    private ConnectionsImpl<BGSMessage> currentServerConnections;
    private final BGSUsers currentServerUsers;
    private boolean shouldTerminate;
    private static final ConcurrentHashMap<Integer,String> connectionIDsAndLoggedInUsers
             = new ConcurrentHashMap<>(); // a static field for the maps of the connections that have a user logged in

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

        if (currentOpCode == 1){ // RegisterMessage

            User userToRegister = ((RegisterMessage) message).getUserToRegister();

            synchronized (currentServerUsers) { // synchronize the database to prevent users with the same name to register concurrently

                // checks if the user is already registered or a password of zero length
                if (currentServerUsers.isUserRegistered(userToRegister) || userToRegister.getPassword().length() == 0)
                    sendError(currentOpCode); // sends an Error message for the output
                else { // the user is not currently registered
                    currentServerUsers.registerUser(userToRegister); // register the user
                    sendRegularAck(currentOpCode); // create an Ack message for the output
                }
            }
        }
        else if (currentOpCode == 2){ // LoginMessage

            User userToLogin = currentServerUsers.getRegisteredUser(((LoginMessage) message).getUserToLogin().getUsername());

            // synchronize the database during the login procedure to prevent two users
            // from logging in concurrently with the same username or the same client
            synchronized (currentServerUsers) {

                // checks if the user is not registered or there is a user logged into the current connection
                // or if the user is already logged in or typed in a wrong password
                if (userToLogin == null || BGSMessagingProtocol.connectionIDsAndLoggedInUsers.containsKey(connectionId) ||
                        currentServerUsers.isUserLoggedIn(userToLogin) || !currentServerUsers.isPasswordValid(userToLogin))
                    sendError(currentOpCode);
                else {
                    currentServerUsers.logUserIn(userToLogin); // logs the user in
                    BGSMessagingProtocol.connectionIDsAndLoggedInUsers.put(connectionId,userToLogin.getUsername()); // updates the connection's current active user
                    sendRegularAck(currentOpCode);
                    ConcurrentLinkedQueue<BGSSavedMessage> unreadMessages = currentServerUsers.getUserUnreadMessages(userToLogin.getUsername());
                    
                    while (!unreadMessages.isEmpty()) {

                        BGSSavedMessage currentUnreadMessage = unreadMessages.poll();

                        sendNotification(userToLogin, currentServerUsers.getRegisteredUser(currentUnreadMessage.getSenderUsername()),
                                currentUnreadMessage.getMessageType(), currentUnreadMessage.getContent());
                    }
                }
            }
        }
        else if (BGSMessagingProtocol.connectionIDsAndLoggedInUsers.containsKey(connectionId)) { // checks if there's a user logged in to the current connection handler

            User currentActiveUser = currentServerUsers.getRegisteredUser(BGSMessagingProtocol.connectionIDsAndLoggedInUsers.get(connectionId));
            String currentActiveUserName = currentActiveUser.getUsername();

            if (currentOpCode == 3){ // LogoutMessage

                // synchronize the database during the logout procedure to prevent sending messages while performing logout
                synchronized (currentServerUsers) {

                    if (!currentServerUsers.isUserLoggedIn(currentActiveUser))  // checks if the user is not logged in
                        sendError(currentOpCode);
                    else {
                        currentServerUsers.logUserOut(currentActiveUser); // logs the user out
                        BGSMessagingProtocol.connectionIDsAndLoggedInUsers.remove(connectionId); // remove the connection from the active ones
                        if (sendRegularAck(currentOpCode))
                            currentServerConnections.disconnect(connectionId); // disconnects the current connection
                    }
                }
            }
            else if (currentOpCode == 4){ // FollowUnfollowMessage

                FollowUnfollowMessage messageToProcess = (FollowUnfollowMessage) message;
                LinkedList<String> usersToActUpon = messageToProcess.getUsers(), usersSuccessfullyActedUpon = new LinkedList<>();

                if (currentServerUsers.isUserLoggedIn(currentActiveUser)) { // check if the user is logged in

                    // synchronize the database during the follow or unfollow procedure to receive a reliable status
                    // of the users' conditions
                    synchronized (currentServerUsers) {
                        if (messageToProcess.getFollowOrUnfollow() == 0)  // in case the user wants to follow
                            usersSuccessfullyActedUpon = currentServerUsers.followUsers(currentActiveUser, usersToActUpon);
                        else // in case the user wants to unfollow
                            usersSuccessfullyActedUpon = currentServerUsers.unfollowUsers(currentActiveUser, usersToActUpon);
                    }
                }

                if (usersSuccessfullyActedUpon.size() > 0)
                    currentServerConnections.send(connectionId, new AckMessage(currentOpCode, (short) usersSuccessfullyActedUpon.size(),
                            usersSuccessfullyActedUpon));
                else
                    sendError(currentOpCode);
            }
            else if (currentOpCode == 5){ // PostMessage

                PostMessage messageToProcess = (PostMessage) message;

                if (currentServerUsers.isUserLoggedIn(currentActiveUser)) {

                    String postContent = messageToProcess.getContent(); // get the post content
                    LinkedList<String> additionalUsersList = messageToProcess.getAdditionalUsersList();

                    // synchronize the database during the post message procedure to receive a reliable status
                    // of the users' conditions
                    synchronized (currentServerUsers) {

                        LinkedList<User> usersToSendNotification = currentServerUsers.addPostToUserAndHisFollowersList
                                (currentActiveUserName, postContent, additionalUsersList);
                        for (User currentUserToSendNotification : usersToSendNotification)
                            sendNotification(currentUserToSendNotification, currentActiveUser, '1', postContent);
                    }

                    sendRegularAck(currentOpCode);
                }

                else
                    sendError(currentOpCode);
            }
            else if (currentOpCode == 6){ // PMMessage

                PMMessage messageToProcess = (PMMessage) message;
                String receiverUsername = messageToProcess.getReceiverUsername(), content = messageToProcess.getContent();

                if (currentServerUsers.isUserLoggedIn(currentActiveUser) && currentServerUsers.isUserRegistered(receiverUsername)) {

                    // synchronize the database during the post PM procedure to receive a reliable status
                    // of the users' conditions
                    synchronized (currentServerUsers) {

                        currentServerUsers.addPMToSenderAndReceiver(currentActiveUserName, receiverUsername, content);

                        if (currentServerUsers.isUserLoggedIn(receiverUsername))
                            sendNotification(currentServerUsers.getRegisteredUser(receiverUsername),
                                    currentActiveUser, '0', content);
                        else
                            currentServerUsers.addUnreadPM(new PM(currentActiveUser.getUsername(), receiverUsername, content));
                    }

                    sendRegularAck(currentOpCode);
                }
                else
                    sendError(currentOpCode);
            }
            else if (currentOpCode == 7){ // UserListMessage

                LinkedList<String> currentlyRegisteredUsers = currentServerUsers.getRegisteredUsers();

                if (currentServerUsers.isUserLoggedIn(currentActiveUser))
                    currentServerConnections.send(connectionId, new AckMessage(currentOpCode,
                            (short) currentlyRegisteredUsers.size(), currentlyRegisteredUsers));
                else
                    sendError(currentOpCode);

            }
            else if (currentOpCode == 8){ // StatsMessage

                StatsMessage messageToProcess = (StatsMessage) message;
                String userToGetStatsName = messageToProcess.getUsername();

                if (currentServerUsers.isUserRegistered(userToGetStatsName))
                    currentServerConnections.send(connectionId, new AckMessage(currentOpCode, currentServerUsers.getNumOfPosts(userToGetStatsName),
                            currentServerUsers.getNumOfFollowers(userToGetStatsName), currentServerUsers.getNumOfFollowing(userToGetStatsName)));
                else
                    sendError(currentOpCode);
            }
        }
        else // in case the current connection handler does not have a user logged in
            sendError(currentOpCode); // send an error in case there's no client connected
    }

    @Override
    public boolean shouldTerminate() {

        return shouldTerminate;
    }

    private boolean sendRegularAck(short currentOpCode){

        return currentServerConnections.send(connectionId, new AckMessage(currentOpCode));
    }

    private void sendError(short currentOpCode){

        currentServerConnections.send(connectionId, new ErrorMessage(currentOpCode));
    }

    private void sendNotification(User userToSend, User userWhoSends, char messageType, String content){

        currentServerConnections.send(getNumOfConnectionForLoggedInUser(userToSend),
                new NotificationMessage(messageType, userWhoSends.getUsername(), content));
    }

    private int getNumOfConnectionForLoggedInUser(User user){

        int numOfConnection = 0;

        for (int currentConnectionNum : BGSMessagingProtocol.connectionIDsAndLoggedInUsers.keySet())
            if (BGSMessagingProtocol.connectionIDsAndLoggedInUsers.get(currentConnectionNum).equals(user.getUsername())) {

                numOfConnection = currentConnectionNum;
                break;
            }

        return numOfConnection;
    }
}