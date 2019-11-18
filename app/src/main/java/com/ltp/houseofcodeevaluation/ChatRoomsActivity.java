package com.ltp.houseofcodeevaluation;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ltp.houseofcodeevaluation.adapters.ChatRoomRecyclerAdapter;
import com.ltp.houseofcodeevaluation.repository.ChatRoom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class ChatRoomsActivity extends Activity {

    private RecyclerView recyclerView;
    private ChatRoomRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);
        getChatRooms();

    }

    private void getChatRooms() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("chat-rooms").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<ChatRoom> chatRooms = new ArrayList<>();

                        for(DocumentSnapshot doc : task.getResult().getDocuments()) {
                            ChatRoom room = doc.toObject(ChatRoom.class);
                            chatRooms.add(room);
                        }
                        recyclerView = findViewById(R.id.chat_room_recycler);
                        adapter = new ChatRoomRecyclerAdapter(chatRooms);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setAdapter(adapter);
                    }
                });
    }
}
