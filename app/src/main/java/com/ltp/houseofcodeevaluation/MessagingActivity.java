package com.ltp.houseofcodeevaluation;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.ltp.houseofcodeevaluation.adapters.MessagesAdapter;
import com.ltp.houseofcodeevaluation.repository.Message;

import java.util.ArrayList;
import java.util.List;

public class MessagingActivity extends Activity {

    private String currentRoom = "HoC";
    private ImageButton sendButton;
    private RecyclerView recyclerView;
    private MessagesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Working!");
            }
        });
        getMessages();
    }

    private void getMessages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("chat-rooms").document(currentRoom).collection("messages").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<Message> messages = new ArrayList<>();

                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            Message message = doc.toObject(Message.class);
                            System.out.println(doc);
                            messages.add(message);
                        }

                        recyclerView = findViewById(R.id.messageRecycler);
                        adapter = new MessagesAdapter(messages);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setAdapter(adapter);
                    }
                });
    }
}
