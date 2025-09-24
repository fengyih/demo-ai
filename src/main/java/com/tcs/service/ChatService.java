package com.tcs.service;

import com.tcs.model.ChatRequest;
import com.tcs.model.ChatResponse;

public interface ChatService {
    ChatResponse processChatRequest(ChatRequest request);
    String formatMessageTimestamp(java.util.Date timestamp);
    boolean validateMessageContent(String content);
}