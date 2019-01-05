package bgu.spl.net.api;

@SuppressWarnings("WeakerAccess") // suppress weaker access warnings

public class User {

    // fields

    private final String username, password;

    public User(String username, String password) {

        this.username = username;
        this.password = password;
    }

    public String getUsername() {

        return username;
    }

    public String getPassword() {

        return password;
    }
}