package com.ltp.houseofcodeevaluation.repository;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.time.LocalDate;
import java.util.Date;

public class Message {
    private String userName;
    private String avatarUri;
    private String text;
    private Date date;

    public Message() {}

    public Message(String text) {
        for (UserInfo profile : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
            this.userName = profile.getDisplayName();
            this.avatarUri = profile.getPhotoUrl().toString();
        }
        this.text = text;
        this.date = new Date();
    }

    public Message(String userName, String avatarUri, String text, Date date) {
        this.userName = userName;
        this.avatarUri = avatarUri;
        this.text = text;
        this.date = date;
    }

    public String getUserName() {
        return userName;
    }

    public String getAvatarUri() {
        return avatarUri;
    }

    public String getText() {
        return text;
    }

    public Date getDate() {
        return date;
    }
}
