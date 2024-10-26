package com.example.mychatbotapp;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatbotService {

    public interface ChatbotResponseListener {
        void onResponseReceived(String response);
        void onError(String errorMessage);
    }

    @SuppressLint("LogNotTimber")
    public void getChatbotResponse(final String userInput, final ChatbotResponseListener listener) {
        new Thread(() -> {
            String apiUrl = "https://api-inference.huggingface.co/models/NousResearch/Hermes-3-Llama-3.1-8B";

            try {
                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("inputs", userInput);
                jsonBody.put("parameters", new JSONObject()
                        .put("temperature", 0.3)
                        .put("top_p", 0.8)
                        .put("max_tokens", 256)
                        .put("stream", false)); // Disable streaming for now

                RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
                Request request = new Request.Builder()
                        .url(apiUrl)
                        .post(body)
                        .addHeader("Authorization", "Bearer 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")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();

                        try {
                            // Parse as JSONArray first
                            JSONArray jsonArray = new JSONArray(responseBody);

                            // Get the first object in the array (assuming it contains the generated text)
                            if (jsonArray.length() > 0) {
                                JSONObject jsonObject = jsonArray.getJSONObject(0);
                                if (jsonObject.has("generated_text")) {
                                    String generatedText = jsonObject.getString("generated_text");
                                    new Handler(Looper.getMainLooper()).post(() -> listener.onResponseReceived(generatedText));
                                } else {
                                    // Handle case where "generated_text" is missing
                                    new Handler(Looper.getMainLooper()).post(() -> listener.onError("Unexpected JSON structure: 'generated_text' missing"));
                                }
                            } else {
                                // Handle case where the array is empty
                                new Handler(Looper.getMainLooper()).post(() -> listener.onError("Unexpected JSON structure: Empty array"));
                            }

                        } catch (JSONException e) {
                            Log.e("ChatbotService", "Error parsing JSON: " + e.getMessage() + ", Response: " + responseBody);
                            new Handler(Looper.getMainLooper()).post(() -> listener.onError("JSON Parsing Error"));
                        }
                    } else {
                        // Handle API error
                        new Handler(Looper.getMainLooper()).post(() -> listener.onError("API Error: " + response.code()));
                    }
                } catch (IOException e) {
                    Log.e("ChatbotService", "Network error while getting chatbot response", e);
                    new Handler(Looper.getMainLooper()).post(() -> listener.onError("Error: Network issue"));
                }

            } catch (final JSONException e) {
                Log.e("ChatbotService", "Error parsing chatbot response", e);
                new Handler(Looper.getMainLooper()).post(() -> listener.onError("Error: Invalid response format"));
            } catch (final Exception e) {
                Log.e("ChatbotService", "Error in chatbot response!", e);
                CrashReportingTree.log(Log.ERROR, e.getMessage(), e);
                new Handler(Looper.getMainLooper()).post(() -> listener.onError("Error in chatbot response!"));
            }
        }).start();
    }

    static class CrashReportingTree {
        @SuppressWarnings("unused")
        public static void log(int priority, @Nullable String message, @Nullable Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }
            FirebaseCrashlytics.getInstance().recordException(t != null ? t : new Exception(message));
        }
    }
}