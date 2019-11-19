package com.ltp.houseofcodeevaluation.repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

/**
 * Definition of a chat room
 */
public class ChatRoom {
    private String name;
    private String description;
    private int numberOfMessages;

    public ChatRoom() {
    }

    public ChatRoom(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNumberOfMessages(int value) {
        this.numberOfMessages = value;
    }

    public int getNumberOfMessages() {
        return numberOfMessages;
    }
}
