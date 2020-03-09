package ca.uwaterloo.io.model;

public class User {
    private static String username;
    private static String authentication;
    private static boolean isOffline;

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        User.username = username;
    }

    public static void setIsOffline(boolean isOffline) {
        User.isOffline = isOffline;
    }

    public static String getAuthentication() {
        return authentication;
    }

    public static void setAuthentication(String authentication) {
        User.authentication = authentication;
    }

    public static boolean getIsOffline() {
        return  User.isOffline;
    }
}
