package com.the_finder_group.tutorfinder.ConnManager;

/**
 * Definicio dels atributs de l'objecte user
 * @author José Luis Puentes Jiménez <jlpuentes74@gmail.com>
 */
public class UserDTO {

    public int userId;
    private String userName;
    private String userMail;
    private String userPswd;
    private String userRol;

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserMail() {
        return userMail;
    }

    public String getUserPswd() {
        return userPswd;
    }

    public String getUserRol() {
        return userRol;
    }


    public void setUserId(int userId) { this.userId = userId; }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserMail(String userMail) {
        this.userMail = userMail;
    }

    public void setUserPswd(String userPswd) {
        this.userPswd = userPswd;
    }

    public void setUserRol(String userRol) {
        this.userRol = userRol;
    }

    //TODO: Hacer toString()

}
