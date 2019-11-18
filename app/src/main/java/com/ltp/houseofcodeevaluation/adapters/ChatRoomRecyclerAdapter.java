package com.ltp.houseofcodeevaluation.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ltp.houseofcodeevaluation.ChatRoomsActivity;
import com.ltp.houseofcodeevaluation.MessagingActivity;
import com.ltp.houseofcodeevaluation.R;
import com.ltp.houseofcodeevaluation.repository.ChatRoom;

import java.util.List;

public class ChatRoomRecyclerAdapter extends RecyclerView.Adapter<ChatRoomRecyclerAdapter.ChatRoomViewHolder> {

    List<ChatRoom> chatRooms;

    public ChatRoomRecyclerAdapter(List<ChatRoom> chatRooms) {
        this.chatRooms = chatRooms;
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatroom, parent, false);

        return new ChatRoomViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoom chatRoom = chatRooms.get(position);
        holder.name.setText(chatRoom.getName());
        holder.name.setOnClickListener(new ItemClick());
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    public static class ChatRoomViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.room_name);
        }
    }

    public static class ItemClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), MessagingActivity.class);
            v.getContext().startActivity(intent);
        }
    }
}
