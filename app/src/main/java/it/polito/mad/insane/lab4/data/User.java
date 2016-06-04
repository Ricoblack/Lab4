package it.polito.mad.insane.lab4.data;


import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import it.polito.mad.insane.lab4.managers.Cryptography;

/**
 * Created by carlocaramia on 25/05/16.
 */
public class User
{
    private String ID;
    private String username;
    private String password;

//    private String userSurname;
//    private Map<String,String> reviewsIdList;
//    private Map<String,String> bookingsIdList;
//    private Map<String,String> favouritesIdList;

    public User()
    {

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        Cryptography cryptography = new Cryptography();
        this.password = cryptography.SHA1(password);
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
//    public User(String userId, String password, String userName, String userSurname, Map<String, String> reviewsIdList, Map<String,
//            String> bookingsIdList, Map<String, String> favouritesIdList) throws NoSuchAlgorithmException, UnsupportedEncodingException {
//        Cryptography cryptography = new Cryptography();
////        this.userId=userId;
//        this.password = cryptography.SHA1(password);
//        this.username = userName;
////        this.reviewsIdList = reviewsIdList;
////        this.bookingsIdList = bookingsIdList;
////        this.favouritesIdList = favouritesIdList;
//
//    }

}
