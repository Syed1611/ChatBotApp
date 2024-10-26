package com.example.mychatbotapp;

public class ChatMessage {
    public String sender; // "user" or "ai"
    public String message;
    public int profileImage; // Resource ID for profile image
    public long timestamp;
    public enum MessageStatus {
        SENDING, SENT, RECEIVED, ERROR
    }
    public MessageStatus status;

    public ChatMessage(String sender, String message, int profileImage, MessageStatus status) {
        this.sender = sender;
        this.message = message;
        this.profileImage = profileImage;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }
    public void setContent(String content, MessageStatus status) {
        this.message = content;
        this.status = status;
    }
}