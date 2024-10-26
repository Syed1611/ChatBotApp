package com.example.mychatbotapp;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final List<ChatMessage> messages;
    private final Context context;

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        // Set profile image (if applicable)
        holder.profileImageView.setImageResource(message.profileImage);

        // Set message content and styling based on sender
        if (message.sender.equals("user")) {
            // User message
            holder.profileImageView.setVisibility(View.GONE); // Hide profile image for user
            holder.messageTextView.setBackgroundResource(R.drawable.chat_bubble_user);
            holder.messageTextView.setText(message.message);

            // Set sent status visibility
            holder.sentStatusImageView.setVisibility(message.status == ChatMessage.MessageStatus.SENT ? View.VISIBLE : View.GONE);

            // Align message to the right
            LinearLayout messageLayout = holder.itemView.findViewById(R.id.messageLayout); // Get the LinearLayout
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) messageLayout.getLayoutParams();
            layoutParams.gravity = Gravity.END;
            messageLayout.setLayoutParams(layoutParams);
        } else {
            // AI message
            holder.profileImageView.setVisibility(View.VISIBLE); // Show profile image for AI

            // Set message background and text color based on night mode
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                holder.messageTextView.setBackgroundResource(R.drawable.chat_bubble_ai_dark);
                holder.messageTextView.setTextColor(ContextCompat.getColor(context, R.color.white));
            } else {
                holder.messageTextView.setBackgroundResource(R.drawable.chat_bubble_ai);
                holder.messageTextView.setTextColor(ContextCompat.getColor(context, R.color.black));
            }

            // Handle message status (sending, received, error)
            switch (message.status) {
                case SENDING:
                    holder.progressBar.setVisibility(View.VISIBLE);
                    holder.messageTextView.setVisibility(View.GONE);
                    holder.errorIcon.setVisibility(View.GONE);
                    break;
                case RECEIVED:
                    holder.progressBar.setVisibility(View.GONE);
                    holder.messageTextView.setVisibility(View.VISIBLE);
                    holder.errorIcon.setVisibility(View.GONE);
                    holder.messageTextView.setText(message.message);
                    break;
                case ERROR:
                    holder.progressBar.setVisibility(View.GONE);
                    holder.messageTextView.setVisibility(View.GONE);
                    holder.errorIcon.setVisibility(View.VISIBLE);
                    break;
            }

            // Align message to the left
            LinearLayout messageLayout = holder.itemView.findViewById(R.id.messageLayout); // Get the LinearLayout
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) messageLayout.getLayoutParams();
            layoutParams.gravity = Gravity.START;
            messageLayout.setLayoutParams(layoutParams);
        }
    }

    // Method to update an existing message
    public void updateMessage(int position, String newContent, ChatMessage.MessageStatus status) {
        if (position >= 0 && position < messages.size()) {
            messages.get(position).setContent(newContent, status);
            notifyItemChanged(position); // Notify the adapter of the change
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profileImageView;
        public TextView messageTextView;
        public ProgressBar progressBar;
        public ImageView errorIcon;
        public ImageView sentStatusImageView; // Add sent status ImageView

        public ViewHolder(View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            progressBar = itemView.findViewById(R.id.progress_bar);
            errorIcon = itemView.findViewById(R.id.error_icon);
            sentStatusImageView = itemView.findViewById(R.id.sent_status_image_view); // Initialize
        }
    }
}