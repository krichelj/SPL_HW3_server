package bgu.spl.net.api;

import bgu.spl.net.api.BGSSavedMessages.PM;
import bgu.spl.net.api.BGSSavedMessages.Post;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("WeakerAccess") // suppress weaker access warnings

public class BGSUsers {

    private final ConcurrentHashMap<String,User> registeredUsers, loggedInUsers;
    private final ConcurrentHashMap<User,LinkedList<User>> registeredUsersAndFollowings, registeredUsersAndFollowers;
    private final ConcurrentHashMap<User,LinkedList<BGSSavedMessage>> registeredUsersAndPostedPosts, registeredUsersAndReceivedPosts,
        registeredUsersAndSentPMS, registeredUsersAndReceivedPMS;

    // constructor

    public BGSUsers() {

        registeredUsers = new ConcurrentHashMap<>();
        loggedInUsers = new ConcurrentHashMap<>();
        registeredUsersAndFollowings = new ConcurrentHashMap<>();
        registeredUsersAndFollowers = new ConcurrentHashMap<>();
        registeredUsersAndPostedPosts = new ConcurrentHashMap<>();
        registeredUsersAndReceivedPosts = new ConcurrentHashMap<>();
        registeredUsersAndSentPMS = new ConcurrentHashMap<>();
        registeredUsersAndReceivedPMS = new ConcurrentHashMap<>();
    }

    // methods

    public void registerUser(User userToRegister){

        registeredUsers.put(userToRegister.getUsername(),userToRegister);
        registeredUsersAndFollowings.put(userToRegister,new LinkedList<>());
        registeredUsersAndFollowers.put(userToRegister,new LinkedList<>());
        registeredUsersAndPostedPosts.put(userToRegister,new LinkedList<>());
        registeredUsersAndReceivedPosts.put(userToRegister,new LinkedList<>());
        registeredUsersAndSentPMS.put(userToRegister,new LinkedList<>());
        registeredUsersAndReceivedPMS.put(userToRegister,new LinkedList<>());
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

        LinkedList<User> currentlyFollowedUsers = registeredUsersAndFollowings.get(followingUser);
        LinkedList<String> addedUsersList = new LinkedList<>();

        for (String currentUserToFollowName : usersToFollow) {

            User currentUserToFollow = registeredUsers.get(currentUserToFollowName); // gets the current user from the user map

            if (!currentlyFollowedUsers.contains(currentUserToFollow)) { // checks if the followingUser is not following the currentUser
                currentlyFollowedUsers.add(currentUserToFollow);
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


    public void addPostToUserAndHisFollowersList(String posterUsername, String postContent){

        User userWhoPosted = registeredUsers.get(posterUsername);
        Post post = new Post(posterUsername, postContent);

        registeredUsersAndPostedPosts.get(userWhoPosted).add(post); // add the post to user's posted posts list
        for (User currentFollowingUser : registeredUsersAndFollowers.get(userWhoPosted))
            registeredUsersAndReceivedPosts.get(currentFollowingUser).add(post); // add the post to the user's followers
    }

    public void addPostFromPosterToAdditionalUsers(String posterUsername, String postContent, LinkedList<String> additionalUsersList){

        Post post = new Post(posterUsername, postContent);

        for (String currentAdditionalUserName : additionalUsersList) // add the post to the additional users
            registeredUsersAndReceivedPosts.get(registeredUsers.get(currentAdditionalUserName)).add(post);
    }

    public void addPMToSenderAndReceiver(String senderUsername, String receiverUsername, String content){

        PM pm = new PM (senderUsername, receiverUsername, content);

        registeredUsersAndSentPMS.get(registeredUsers.get(senderUsername)).add(pm);
        registeredUsersAndReceivedPMS.get(registeredUsers.get(receiverUsername)).add(pm);
    }

    public LinkedList<String> getRegisteredUsers(){

        return new LinkedList<>(registeredUsers.keySet());
    }

    public int getNumOfPosts(String username) {

        return registeredUsersAndPostedPosts.get(registeredUsers.get(username)).size();
    }

    public int getNumOfFollowers(String username) {

        return registeredUsersAndFollowers.get(registeredUsers.get(username)).size();
    }

    public int getNumOfFollowing(String username) {

        return registeredUsersAndFollowings.get(registeredUsers.get(username)).size();
    }
}