package bgu.spl.net.api;

import bgu.spl.net.api.BGSSavedMessages.PM;
import bgu.spl.net.api.BGSSavedMessages.Post;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@SuppressWarnings("WeakerAccess") // suppress weaker access warnings

public class BGSUsers {

    // all maps are concurrent and therefore WE DO NOT HAVE TO WORRY ABOUT CONCURRENCY

    private final Map<String, User> registeredUsers;
    private final ConcurrentHashMap<String,User> loggedInUsers;
    private final ConcurrentHashMap<User,LinkedList<User>> registeredUsersAndFollowings, registeredUsersAndFollowers;
    private final ConcurrentHashMap<User,LinkedList<BGSSavedMessage>> registeredUsersAndSentMessages, registeredUsersAndReceivedMessages;
    private final ConcurrentHashMap<User,ConcurrentLinkedQueue<BGSSavedMessage>> registeredUsersAndUnreadMessages;

    // constructor

    public BGSUsers() {

        // make the registered users concurrent and linked to support thread-safety and order
        registeredUsers = Collections.synchronizedMap(new LinkedHashMap<>());
        loggedInUsers = new ConcurrentHashMap<>();
        registeredUsersAndFollowings = new ConcurrentHashMap<>();
        registeredUsersAndFollowers = new ConcurrentHashMap<>();
        registeredUsersAndSentMessages = new ConcurrentHashMap<>();
        registeredUsersAndReceivedMessages = new ConcurrentHashMap<>();
        registeredUsersAndUnreadMessages = new ConcurrentHashMap<>();
    }

    // methods

    public void registerUser(User userToRegister){

        registeredUsers.put(userToRegister.getUsername(),userToRegister);
        registeredUsersAndFollowings.put(userToRegister,new LinkedList<>());
        registeredUsersAndFollowers.put(userToRegister,new LinkedList<>());
        registeredUsersAndSentMessages.put(userToRegister,new LinkedList<>());
        registeredUsersAndReceivedMessages.put(userToRegister,new LinkedList<>());
        registeredUsersAndUnreadMessages.put(userToRegister,new ConcurrentLinkedQueue<>());
    }

    public void logUserIn(User userToLogIn){

        loggedInUsers.put(userToLogIn.getUsername(),userToLogIn);
    }

    public boolean isUserRegistered(String username){

        return registeredUsers.containsKey(username);
    }

    public boolean isUserRegistered(User user){

        return user!= null && registeredUsers.containsKey(user.getUsername());
    }

    public boolean isUserLoggedIn(String username){

        return isUserRegistered(username) && loggedInUsers.containsKey(username);
    }

    public boolean isUserLoggedIn(User user){

        return isUserRegistered(user) && loggedInUsers.containsKey(user.getUsername());
    }

    public void logUserOut(User user){

        loggedInUsers.remove(user.getUsername());
    }

    public boolean isPasswordValid(User user){

        return registeredUsers.get(user.getUsername()).getPassword().equals(user.getPassword());
    }

    public LinkedList<String> followUsers(User followingUser, LinkedList<String> usersToFollow){

        LinkedList<String> addedUsersList = new LinkedList<>();
        followingUser = registeredUsers.get(followingUser.getUsername()); // update the following user

        for (String currentUserToFollowName : usersToFollow) {

            User currentUserToFollow = registeredUsers.get(currentUserToFollowName); // gets the current user from the user map

            if (currentUserToFollow != null && !isUserFollowingOtherUser(followingUser,currentUserToFollow)) { // checks if the followingUser is not following the currentUser
                registeredUsersAndFollowings.get(followingUser).add(currentUserToFollow); // add the currentUserToFollow to the followingUser's following list
                registeredUsersAndFollowers.get(currentUserToFollow).add(followingUser); // add the followingUser to the currentUserToFollow's followers list
                addedUsersList.add(currentUserToFollowName);
            }
        }

        return addedUsersList;
    }

    public LinkedList<String> unfollowUsers(User unfollowingUser, LinkedList<String> usersToUnfollow) {

        LinkedList<String> removedUsersList = new LinkedList<>();
        unfollowingUser = registeredUsers.get(unfollowingUser.getUsername());

        for (String currentUserToUnfollowName : usersToUnfollow) {

            User currentUserToUnfollow = registeredUsers.get(currentUserToUnfollowName); // gets the current user from the user map

            if (isUserFollowingOtherUser(unfollowingUser,currentUserToUnfollow)) { // checks if the followingUser is following the currentUser
                registeredUsersAndFollowings.get(unfollowingUser).remove(currentUserToUnfollow);
                registeredUsersAndFollowers.get(currentUserToUnfollow).remove(unfollowingUser);
                removedUsersList.add(currentUserToUnfollowName);
            }
        }

        return removedUsersList;
    }

    public LinkedList<User> addPostToUserAndHisFollowersList(String posterUsername, String postContent, LinkedList<String> additionalUsersList){

        User userWhoPosted = registeredUsers.get(posterUsername);
        Post post = new Post(posterUsername, postContent);
        LinkedList<User> userNamesToSendNotification = addPostToUserFollowersList(userWhoPosted, post); //

        registeredUsersAndSentMessages.get(userWhoPosted).add(post); // add the post to user's posted posts list

        if (additionalUsersList!= null && !additionalUsersList.isEmpty()) // send the post to the additional users
            for (String currentAdditionalUserName : additionalUsersList) {

                User currentAdditionalUser = registeredUsers.get(currentAdditionalUserName);

                if (currentAdditionalUser!= null &&  // checks if the currentAdditionalUser isn't following the userWhoPosted
                        !isUserFollowingOtherUser(currentAdditionalUser, userWhoPosted)) {

                    registeredUsersAndReceivedMessages.get(registeredUsers.get(currentAdditionalUserName)).add(post);

                    if (!isUserLoggedIn(currentAdditionalUser)) // check if the current following user is not logged in
                        registeredUsersAndUnreadMessages.get(currentAdditionalUser).add(post); // add the post to all users who follow that are not logged in
                    else // in case the current following user is logged in
                        userNamesToSendNotification.add(currentAdditionalUser); // add him to the users to send a notification to
                }
            }


        return userNamesToSendNotification;
    }

    private LinkedList<User> addPostToUserFollowersList(User userWhoPosted, Post post){

        LinkedList<User> userNamesToSendNotification = new LinkedList<>();

        for (User currentFollowingUser : registeredUsersAndFollowers.get(userWhoPosted)) {

            registeredUsersAndReceivedMessages.get(currentFollowingUser).add(post); // add the post to the user

            if (!isUserLoggedIn(currentFollowingUser))
                registeredUsersAndUnreadMessages.get(currentFollowingUser).add(post);
            else
                userNamesToSendNotification.add(currentFollowingUser);
        }

        return userNamesToSendNotification;
    }

    public void addUnreadPM(PM pm){

        registeredUsersAndUnreadMessages.get(registeredUsers.get(pm.getReceiverUsername())).add(pm);
    }

    public ConcurrentLinkedQueue<BGSSavedMessage> getUserUnreadMessages(String username){

        return registeredUsersAndUnreadMessages.get(registeredUsers.get(username));
    }

    private boolean isUserFollowingOtherUser(User followingUser, User otherUser){

        return registeredUsersAndFollowings.get(registeredUsers.get(followingUser.getUsername())).contains(otherUser);
    }

    public void addPMToSenderAndReceiver(String senderUsername, String receiverUsername, String content){

        PM pm = new PM (senderUsername, receiverUsername, content);

        registeredUsersAndSentMessages.get(registeredUsers.get(senderUsername)).add(pm);
        registeredUsersAndReceivedMessages.get(registeredUsers.get(receiverUsername)).add(pm);
    }

    public User getRegisteredUser(String username){

        return registeredUsers.get(username);
    }
    public LinkedList<String> getRegisteredUsers(){

        return new LinkedList<>(registeredUsers.keySet());
    }

    public short getNumOfPosts(String username) {

        int numOfPosts = 0;

        for (BGSSavedMessage currentMessage : registeredUsersAndSentMessages.get(registeredUsers.get(username)))
            if (currentMessage instanceof Post)
                numOfPosts++;

        return (short) numOfPosts;
    }

    public short getNumOfFollowers(String username) {

        return (short) registeredUsersAndFollowers.get(registeredUsers.get(username)).size();
    }

    public short getNumOfFollowing(String username) {

        return (short) registeredUsersAndFollowings.get(registeredUsers.get(username)).size();
    }
}