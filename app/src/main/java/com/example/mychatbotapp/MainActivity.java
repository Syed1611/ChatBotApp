package com.example.mychatbotapp;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText userInput;
    private ImageButton sendButton; // Make sendButton a member variable
    private List<ChatMessage> messages;
    private ChatAdapter chatAdapter;
    private ChatbotService chatbotService;
    private StringBuilder currentAiMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        userInput = findViewById(R.id.userInput);
        sendButton = findViewById(R.id.sendButton); // Initialize sendButton

        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messages);
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatbotService = new ChatbotService();
        currentAiMessage = new StringBuilder();

        sendButton.setOnClickListener(v -> {
            String input = userInput.getText().toString();
            if (!input.isEmpty()) {
                sendMessage(input);
                userInput.setText("");
            } else {
                Toast.makeText(MainActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ... (onCreateOptionsMenu and onOptionsItemSelected remain the same) ...

    private void sendMessage(String message) {
        messages.add(new ChatMessage("user", message, R.drawable.user_profile_image, ChatMessage.MessageStatus.SENT));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        chatRecyclerView.scrollToPosition(messages.size() - 1);

        currentAiMessage.setLength(0); // Reset StringBuilder for new response

        // Add a placeholder message for the AI's response with initial status
        messages.add(new ChatMessage("ai", "", R.drawable.ai_profile_image, ChatMessage.MessageStatus.SENDING));
        int aiMessageIndex = messages.size() - 1;
        chatAdapter.notifyItemInserted(aiMessageIndex);
        chatRecyclerView.scrollToPosition(aiMessageIndex);

        sendButton.setEnabled(false); // Disable the send button

        chatbotService.getChatbotResponse(message, new ChatbotService.ChatbotResponseListener() {
            @Override
            public void onResponseReceived(String responseChunk) {
                runOnUiThread(() -> {
                    currentAiMessage.append(responseChunk);
                    chatAdapter.updateMessage(aiMessageIndex, currentAiMessage.toString(), ChatMessage.MessageStatus.RECEIVED);
                    sendButton.setEnabled(true); // Re-enable the send button
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    chatAdapter.updateMessage(aiMessageIndex, "Error: " + errorMessage, ChatMessage.MessageStatus.ERROR);
                    sendButton.setEnabled(true); // Re-enable the send button
                    // You can add more specific error handling here, like displaying a Toast or a dialog
                    Toast.makeText(MainActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}