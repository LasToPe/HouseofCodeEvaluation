package com.ltp.houseofcodeevaluation.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ltp.houseofcodeevaluation.R;
import com.ltp.houseofcodeevaluation.repository.Message;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Adapter for the message view
 */
public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder> {

    List<Message> messages;
    ViewGroup parent;

    public MessagesAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.partial_message, parent, false);

        return new MessagesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.userName.setText(message.getUserName());
        Picasso.with(parent.getContext()).load(message.getAvatarUri()).into(holder.avatar);
//        holder.avatar.setImageURI(Uri.parse(message.getAvatarUri()));
        holder.message.setText(message.getText());
        holder.date.setText(message.getDate().toString());
        Picasso.with(parent.getContext()).load(message.getImageUri()).into(holder.imageView);
//        holder.imageView.setImageURI(message.getImageUri());
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class MessagesViewHolder extends RecyclerView.ViewHolder {

        ImageView avatar;
        TextView userName;
        TextView message;
        TextView date;
        ImageView imageView;
        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatarView);
            userName = itemView.findViewById(R.id.userNameView);
            message = itemView.findViewById(R.id.messageView);
            date = itemView.findViewById(R.id.dateView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
