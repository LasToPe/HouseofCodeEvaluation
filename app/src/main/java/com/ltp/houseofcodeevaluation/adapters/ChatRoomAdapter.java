package com.ltp.houseofcodeevaluation.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ltp.houseofcodeevaluation.MessagingActivity;
import com.ltp.houseofcodeevaluation.R;
import com.ltp.houseofcodeevaluation.repository.ChatRoom;

import java.util.List;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    List<ChatRoom> chatRooms;

    public ChatRoomAdapter(List<ChatRoom> chatRooms) {
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
        holder.name.setOnClickListener(new ItemClick(holder.name.getText().toString()));

        holder.chevron.setOnClickListener(new ChevronClick(holder.description, holder.messages));
        holder.description.setText(chatRoom.getDescription());
        String messageString = "Messages: " + chatRoom.getNumberOfMessages();
        holder.messages.setText(messageString);
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    public static class ChatRoomViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView description;
        TextView messages;
        ImageView chevron;
        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.room_name);
            description = itemView.findViewById(R.id.room_description);
            messages = itemView.findViewById(R.id.messages_in_room);
            chevron = itemView.findViewById(R.id.chevron);
        }
    }

    public static class ItemClick implements View.OnClickListener {

        private String name;
        public ItemClick(String name) {
            this.name = name;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), MessagingActivity.class);
            intent.putExtra("currentRoom", name);
            v.getContext().startActivity(intent);

        }
    }

    public static class ChevronClick implements View.OnClickListener {

        private TextView description;
        private TextView messages;

        public ChevronClick(TextView description, TextView messages) {
            this.description = description;
            this.messages = messages;
        }

        @Override
        public void onClick(View v) {
            if(description.getVisibility() == View.GONE) {
                description.setVisibility(View.VISIBLE);
                messages.setVisibility(View.VISIBLE);
            } else {
                description.setVisibility(View.GONE);
                messages.setVisibility(View.GONE);
            }
        }
    }
}
