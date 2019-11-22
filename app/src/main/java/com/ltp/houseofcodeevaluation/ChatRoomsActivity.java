package com.ltp.houseofcodeevaluation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.ltp.houseofcodeevaluation.adapters.ChatRoomAdapter;
import com.ltp.houseofcodeevaluation.repository.ChatRoom;

import java.util.ArrayList;
import java.util.List;


public class ChatRoomsActivity extends Activity {

    private RecyclerView recyclerView;
    private ChatRoomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        // Set up refresh view
        final SwipeRefreshLayout layout = findViewById(R.id.refreshView);
        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getChatRooms();
                layout.setRefreshing(false);
            }
        });

        // Set up 'menu' dialog
        ImageView menu = findViewById(R.id.menuImage);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Menu();
            }
        });

        getChatRooms();
    }

    /**
     * Method for getting the chat rooms from firebase.
     * After getting the chat rooms, the recycler is filled with the data
     */
    private void getChatRooms() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            db.collection("chat-rooms").orderBy("newestMessage", Query.Direction.DESCENDING).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<ChatRoom> chatRooms = new ArrayList<>();

                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                ChatRoom room = doc.toObject(ChatRoom.class);
                                chatRooms.add(room);
                            }
                            // Fill recycler
                            recyclerView = findViewById(R.id.chat_room_recycler);
                            adapter = new ChatRoomAdapter(chatRooms);
                            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                            recyclerView.setLayoutManager(layoutManager);
                            recyclerView.setAdapter(adapter);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
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
     * Create and show error dialog
     */
    private void Error() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("An error occurred while getting the chat rooms, please try again later");
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Create and show simple dialog to allow the user to log out.
     */
    private void Menu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Log out?");
        builder.setPositiveButton("Log out", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AuthUI.getInstance().signOut(getApplicationContext())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                finish();
                            }
                        });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
