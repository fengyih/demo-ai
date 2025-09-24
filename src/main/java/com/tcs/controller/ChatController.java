package com.tcs.controller;

import com.tcs.model.ChatRequest;
import com.tcs.model.ChatResponse;
import com.tcs.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // 处理聊天请求
    @PostMapping
    public ResponseEntity<ChatResponse> processChatRequest(@RequestBody ChatRequest request) {
        try {
            ChatResponse response = chatService.processChatRequest(request);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            System.err.println("Error processing chat: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChatResponse(false, null, "处理聊天请求失败"));
        }
    }
}