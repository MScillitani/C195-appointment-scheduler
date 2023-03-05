package models;

/** has user setters/getters */
public class User {
    private static int userId;
    private static String username;
    private String password;
    /** gets user id */
    public static int getUserId() {
        return userId;
    }
    /** sets user id */
    public void setUserId(int userId) {
        this.userId = userId;
    }
    /** gets user name */
    public static String getUsername() {
        return username;
    }
    /** sets user name */
    public void setUsername(String username) {
        this.username = username;
    }
    /** gets user pass */
    public String getPassword() {
        return password;
    }
    /** sets user pass */
    public void setPassword(String password) {
        this.password = password;
    }
}