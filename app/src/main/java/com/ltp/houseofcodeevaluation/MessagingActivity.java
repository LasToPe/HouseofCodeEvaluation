package com.ltp.houseofcodeevaluation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ltp.houseofcodeevaluation.adapters.MessagesAdapter;
import com.ltp.houseofcodeevaluation.repository.Message;

import java.util.ArrayList;
import java.util.List;

public class MessagingActivity extends Activity {

    private String currentRoom;
    private TextView messageText;
    private ImageButton sendButton;
    private RecyclerView recyclerView;
    private MessagesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        currentRoom = getIntent().getStringExtra("currentRoom");

        messageText = findViewById(R.id.messageText);
        sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        getMessages();
        attachListener();
    }

    private void getMessages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            CollectionReference messageRef = db.collection("chat-rooms").document(currentRoom).collection("messages");
            messageRef.orderBy("date").limit(50).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<Message> messages = new ArrayList<>();

                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                Message message = doc.toObject(Message.class);
                                messages.add(message);
                            }

                            postToView(messages);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.wtf("Error", e);
                    Error();
                }
            });
        } catch (Exception e) {
            Log.wtf("Error", e);
            Error();
        }
    }

    private void attachListener() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            CollectionReference messagesRef = db.collection("chat-rooms").document(currentRoom).collection("messages");
            messagesRef.orderBy("date")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if(e != null) {
                                Log.wtf("Error", e);
                            }

                            System.out.println("EVENT!");

                            List<Message> messages = new ArrayList<>();
                            for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                Message message = doc.toObject(Message.class);
                                messages.add(message);
                            }
                            postToView(messages);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void postToView(List<Message> messages) {
        recyclerView = findViewById(R.id.messageRecycler);
        adapter = new MessagesAdapter(messages);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        ((LinearLayoutManager)layoutManager).setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void sendMessage() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String text = messageText.getText().toString();
        messageText.setText("");
        Message message = new Message(text);
        try {
            db.collection("chat-rooms").document(currentRoom).collection("messages").add(message);
        } catch (Exception e) {
            Log.wtf("Error", e);
        }
    }

    private void Error() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("An error occurred while getting the messages, please try again later");
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
