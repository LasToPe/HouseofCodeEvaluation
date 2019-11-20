package com.ltp.houseofcodeevaluation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ltp.houseofcodeevaluation.adapters.MessagesAdapter;
import com.ltp.houseofcodeevaluation.repository.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessagingActivity extends Activity {

    private String currentRoom;
    private TextView messageText;
    private ImageButton sendButton;
    private RecyclerView recyclerView;
    private ImageButton photoButton;
    private MessagesAdapter adapter;
    private List<Message> messageList;
    private int PICK_IMAGE_REQUEST = 55;
    private String filepath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        // Sets the current chat room from the extras strings
        currentRoom = getIntent().getStringExtra("currentRoom");

        // Set up the messaging text box and send button
        messageText = findViewById(R.id.messageText);
        sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        photoButton = findViewById(R.id.photoButton);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        getMessages();
        attachListener();
    }

    /**
     * Get the initial messages of the chat room from firebase
     */
    private void getMessages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            CollectionReference messageRef = db.collection("chat-rooms").document(currentRoom).collection("messages");
            messageRef.orderBy("date").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<Message> messages = new ArrayList<>();

                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                Message message = doc.toObject(Message.class);
                                messages.add(message);
                            }

                            messageList = messages;
                            postToView(messages);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("Error", e.getMessage());
                    Error();
                }
            });
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            Error();
        }
    }

    /**
     * Create and attach listener for changes in the messages collection for the current chat room
     */
    private void attachListener() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            CollectionReference messagesRef = db.collection("chat-rooms").document(currentRoom).collection("messages");
            messagesRef.orderBy("date")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if(e != null) {
                                Log.e("Error", e.getMessage());
                            }

                            List<Message> messages = new ArrayList<>();
                            for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                Message message = doc.toObject(Message.class);
                                messages.add(message);
                            }
                            messageList = messages;
                            postToView(messages);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load the messages into the view
     * @param messages List of the loaded messages
     */
    private void postToView(List<Message> messages) {
        recyclerView = findViewById(R.id.messageRecycler);
        adapter = new MessagesAdapter(messages);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        ((LinearLayoutManager)layoutManager).setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Method for sending a message, gets the string from the text field creates a new message with the content.
     * Sends the data to the firebase and updates the number of messages in the current room.
     */
    private void sendMessage() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        String text = messageText.getText().toString();
        if (text.equals("") && filepath == null) {
            return;
        }

        HandleSubscription();

        try {
            Message message = new Message(text);
            db.collection("chat-rooms").document(currentRoom).collection("messages").add(message).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    uploadImage(documentReference);
                }
            });
            db.collection("chat-rooms").document(currentRoom).update("numberOfMessages", messageList.size() + 1);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        } finally {
            messageText.setText("");
            findViewById(R.id.preview).setVisibility(View.GONE);
        }
    }

    /**
     * Method for handling subscribing to a chat room, shows a dialog to subscribe and handles in the background
     */
    private void HandleSubscription() {
        // Trim the current room string, topics cannot contain spaces, must follow [a-zA-Z0-9-_.~%]
        final String trimmedRoomName = currentRoom.replace(" ", "_").replace(".", "");
        // Get whether or not the user is currently subscribed from the shared preferences
        SharedPreferences preferences = getSharedPreferences("SubscribedTopics", MODE_PRIVATE);
        boolean subscribed = preferences.getBoolean(currentRoom, false);

        // If the user isn't subscribed and posts in the room the user will be asked if they want to subscribe
        if(!subscribed) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Would you like to subscribe to ".concat(currentRoom).concat("?"));
            builder.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FirebaseMessaging.getInstance().subscribeToTopic(trimmedRoomName)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(!task.isSuccessful()) {
                                        Log.e("Error", "Something went wrong subscribing to the topic...");
                                    } else {
                                        Log.i("Info","Subscribed to " + currentRoom);
                                        // Update shared preferences
                                        SharedPreferences preferences = getSharedPreferences("SubscribedTopics", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.putBoolean(currentRoom, true);
                                        editor.apply();
                                    }
                                }
                            });
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filepath = data.getData().toString();
            ImageView preview = findViewById(R.id.preview);
            preview.setImageURI(data.getData());
            preview.setVisibility(View.VISIBLE);
        }
    }

    private void uploadImage(DocumentReference documentReference) {
        try {
            if (filepath != null) {
                Uri path = Uri.parse(filepath);
                FirebaseStorage storage = FirebaseStorage.getInstance();
                final String imagePath = currentRoom + "/" + documentReference.getId();
                final StorageReference reference = storage.getReference().child(imagePath);
                reference.putFile(path).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        Log.d("Debug", task.getResult().toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error", e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            filepath = null;
        }
    }

    /**
     * Creates and shows a dialog in case of a failure
     */
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
