package bgu.spl.net.api;

import bgu.spl.net.api.BGSSavedMessages.PM;
import bgu.spl.net.api.BGSSavedMessages.Post;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@SuppressWarnings("WeakerAccess") // suppress weaker access warnings

public class BGSUsers {

    private final ConcurrentHashMap<String,User> registeredUsers, loggedInUsers;
    private final ConcurrentHashMap<User,LinkedList<User>> registeredUsersAndFollowings, registeredUsersAndFollowers;
    private final ConcurrentHashMap<User,LinkedList<BGSSavedMessage>> registeredUsersAndSentMessages, registeredUsersAndReceivedMessages;
    private final ConcurrentHashMap<User, ConcurrentLinkedQueue<BGSSavedMessage>> registeredUsersAndUnreadMessages;

    // constructor

    public BGSUsers() {

        registeredUsers = new ConcurrentHashMap<>();
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

    public void logUserIn(User userToLogIn){ // synchronize the database to prevent users with the same name to register concurrently

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

        for (String currentUserToFollowName : usersToFollow) {

            User currentUserToFollow = registeredUsers.get(currentUserToFollowName); // gets the current user from the user map

            if (!isUserFollowingOtherUser(followingUser,currentUserToFollow)) { // checks if the followingUser is not following the currentUser
                registeredUsersAndFollowings.get(followingUser).add(currentUserToFollow);
                addedUsersList.add(currentUserToFollowName);
            }
        }

        return addedUsersList;
    }

    public LinkedList<String> unfollowUsers(User unfollowingUser, LinkedList<String> usersToUnfollow) {

        LinkedList<User> currentlyFollowedUsers = registeredUsersAndFollowings.get(unfollowingUser);
        LinkedList<String> removedUsersList = new LinkedList<>();

        for (String currentUserToUnfollowName : usersToUnfollow) {

            User currentUserToUnfollow = registeredUsers.get(currentUserToUnfollowName); // gets the current user from the user map

            if (currentlyFollowedUsers.contains(currentUserToUnfollow)) { // checks if the followingUser is following the currentUser
                currentlyFollowedUsers.remove(currentUserToUnfollow);
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

        if (!additionalUsersList.isEmpty()) // send the post to the additional users
            for (String currentAdditionalUserName : additionalUsersList) {

                User currentAdditionalUser = registeredUsers.get(currentAdditionalUserName);

                if (!isUserFollowingOtherUser(currentAdditionalUser, userWhoPosted)) { // checks if the currentAdditionalUser isn't following the userWhoPosted

                    registeredUsersAndReceivedMessages.get(currentAdditionalUser).add(post);

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

    private boolean isUserFollowingOtherUser(User followingUser, User otherUser){

        LinkedList<User> currentlyFollowedUsers = registeredUsersAndFollowings.get(followingUser);

        return currentlyFollowedUsers!=null && currentlyFollowedUsers.contains(otherUser);
    }


    public void addPMToSenderAndReceiver(String senderUsername, String receiverUsername, String content){

        PM pm = new PM (senderUsername, receiverUsername, content);

        registeredUsersAndSentMessages.get(registeredUsers.get(senderUsername)).add(pm);
        registeredUsersAndReceivedMessages.get(registeredUsers.get(receiverUsername)).add(pm);
    }

    public LinkedList<String> getRegisteredUsers(){

        return new LinkedList<>(registeredUsers.keySet());
    }

    public int getNumOfPosts(String username) {

        int numOfPosts = 0;

        for (BGSSavedMessage currentMessage : registeredUsersAndSentMessages.get(registeredUsers.get(username)))
            if (currentMessage instanceof Post)
                numOfPosts++;

        return numOfPosts;
    }

    public int getNumOfFollowers(String username) {

        return registeredUsersAndFollowers.get(registeredUsers.get(username)).size();
    }

    public int getNumOfFollowing(String username) {

        return registeredUsersAndFollowings.get(registeredUsers.get(username)).size();
    }
}