package ca.uwaterloo.io.model;

public class User {
    private static String username;
    private static String authentication;

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        User.username = username;
    }

    public static String getAuthentication() {
        return authentication;
    }

    public static void setAuthentication(String authentication) {
        User.authentication = authentication;
    }
}
