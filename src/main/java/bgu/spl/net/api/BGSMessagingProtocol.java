package bgu.spl.net.api;

import bgu.spl.net.api.BGSMessages.*;
import bgu.spl.net.api.BGSSavedMessages.PM;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.BlockingConnectionHandler;
import java.util.LinkedList;
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
        BlockingConnectionHandler<BGSMessage> currentConnection = (BlockingConnectionHandler<BGSMessage>) currentServerConnections.getClient(connectionId);

        if (currentOpCode == 1){ // RegisterMessage

            User userToRegister = ((RegisterMessage) message).getUserToRegister();

            // checks if the user is already registered or a password of zero length
            if (currentServerUsers.isUserRegistered(userToRegister) || userToRegister.getPassword().length() == 0)
                sendError(currentOpCode); // sends an Error message for the output
            else { // the user is not currently registered
                currentServerUsers.registerUser(userToRegister); // register the user
                sendRegularAck(currentOpCode); // create an Ack message for the output
            }
        }

        else if (currentOpCode == 2){ // LoginMessage

            User userToLogin = currentServerUsers.getRegisteredUser(((LoginMessage) message).getUserToLogin().getUsername());


            // checks if there is a user logged into the current connection, if the user is already logged in or typed in a wrong password
            if (currentConnection.hasUserLoggedIn() || userToLogin == null ||
                    currentServerUsers.isUserLoggedIn(userToLogin) || !currentServerUsers.isPasswordValid(userToLogin))
                sendError(currentOpCode);
            else {
                currentServerUsers.logUserIn(userToLogin); // logs the user in
                currentServerConnections.logUserIn(connectionId, userToLogin); // updates the connection's current active user
                sendRegularAck(currentOpCode);

                ConcurrentLinkedQueue<BGSSavedMessage> unreadMessages = currentServerUsers.getUserUnreadMessages(userToLogin.getUsername());
                    
                while (!unreadMessages.isEmpty()) {

                    BGSSavedMessage currentUnreadMessage = unreadMessages.poll();

                    sendNotification(userToLogin, currentServerUsers.getRegisteredUser(currentUnreadMessage.getSenderUsername()),
                            currentUnreadMessage.getMessageType(), currentUnreadMessage.getContent());
                }
            }

        }

        else if (currentConnection.hasUserLoggedIn()) { // checks if there's a user logged in to the current connection handler

            User currentActiveUser = currentConnection.getCurrentActiveUser();
            String currentActiveUserName = currentActiveUser.getUsername();

            if (currentOpCode == 3){ // LogoutMessage

                if (!currentServerUsers.isUserLoggedIn(currentActiveUser))  // checks if the user is not logged in
                    sendError(currentOpCode);
                else {
                    currentServerUsers.logUserOut(currentActiveUser); // logs the user out
                    if (sendRegularAck(currentOpCode))
                        currentServerConnections.disconnect(connectionId); // disconnects the current connection
                }
            }

            else if (currentOpCode == 4){ // FollowUnfollowMessage

                FollowUnfollowMessage messageToProcess = (FollowUnfollowMessage) message;
                LinkedList<String> usersToActUpon = messageToProcess.getUsers(), usersSuccessfullyActedUpon = new LinkedList<>();

                if (currentServerUsers.isUserLoggedIn(currentActiveUser)) { // check if the user is logged in

                    if (messageToProcess.getFollowOrUnfollow() == 0)  // in case the user wants to follow
                        usersSuccessfullyActedUpon = currentServerUsers.followUsers(currentActiveUser, usersToActUpon);
                    else // in case the user wants to unfollow
                        usersSuccessfullyActedUpon = currentServerUsers.unfollowUsers(currentActiveUser, usersToActUpon);
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
                    LinkedList<User> usersToSendNotification = currentServerUsers.addPostToUserAndHisFollowersList
                                    (currentActiveUserName, postContent, additionalUsersList);

                    sendRegularAck(currentOpCode);

                    for (User currentUserToSendNotification : usersToSendNotification)
                        sendNotification(currentUserToSendNotification,currentActiveUser,'1',postContent);
                }

                else
                    sendError(currentOpCode);
            }

            else if (currentOpCode == 6){ // PMMessage

                PMMessage messageToProcess = (PMMessage) message;
                String receiverUsername = messageToProcess.getReceiverUsername(), content = messageToProcess.getContent();

                if (currentServerUsers.isUserLoggedIn(currentActiveUser) && currentServerUsers.isUserRegistered(receiverUsername)) {

                    currentServerUsers.addPMToSenderAndReceiver(currentActiveUserName, receiverUsername, content);
                    sendRegularAck(currentOpCode);

                    if (currentServerUsers.isUserLoggedIn(receiverUsername))
                        sendNotification(currentServerUsers.getRegisteredUser(receiverUsername),
                            currentActiveUser,'0',content);
                    else
                        currentServerUsers.addUnreadPM(new PM(currentActiveUser.getUsername(),receiverUsername,content));
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

        currentServerConnections.send(currentServerConnections.getNumOfConnectionForLoggedInUser(userToSend),
                new NotificationMessage(messageType, userWhoSends.getUsername(), content));
    }
}