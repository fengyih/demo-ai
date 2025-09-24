package com.tcs.service;

import com.tcs.model.ChatRequest;
import com.tcs.model.ChatResponse;
import com.tcs.model.Message;
import com.tcs.model.Character;
import com.tcs.repository.CharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ChatServiceImplTest {

    @Mock
    private CharacterRepository characterRepository;

    @Mock
    private ChatClient chatClient;

    @InjectMocks
    private ChatServiceImpl chatService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // 移除mockAiResponse方法，直接在测试中创建更简单的模拟行为
    
    @Test
    public void testProcessChatRequest_Success() {
        // 准备测试数据
        String characterId = "1";
        String userMessage = "你好吗？";
        Character character = new Character();
        character.setId(characterId);
        character.setName("爱因斯坦");
        character.setCategory("科学家");
        character.setDescription("著名物理学家");
        character.setPersonality("聪明、好奇、富有洞察力");

        // 模拟依赖行为
        when(characterRepository.getCharacterById(characterId)).thenReturn(character);
        
        // 简单地模拟chatClient返回一个字符串
        when(chatClient.call(any(Prompt.class))).thenAnswer(invocation -> {
            // 创建一个简单的对象来满足调用链
            Object mockObject = new Object() {
                public Object getResult() {
                    return new Object() {
                        public Object getOutput() {
                            return new Object() {
                                public String getContent() {
                                    return "我很好，很高兴见到你！";
                                }
                            };
                        }
                    };
                }
            };
            return mockObject;
        });

        // 执行测试
        ChatRequest request = new ChatRequest(characterId, userMessage, new ArrayList<>());
        ChatResponse response = chatService.processChatRequest(request);

        // 验证结果
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessage());
        assertEquals("character", response.getMessage().getSender());
        assertNotNull(response.getMessage().getText());
    }

    @Test
    public void testProcessChatRequest_EmptyCharacterId() {
        // 执行测试
        ChatRequest request = new ChatRequest(null, "你好", new ArrayList<>());
        ChatResponse response = chatService.processChatRequest(request);

        // 验证结果
        assertFalse(response.isSuccess());
        assertNotNull(response.getError());
    }

    @Test
    public void testProcessChatRequest_NullMessage() {
        // 执行测试
        ChatRequest request = new ChatRequest("1", null, new ArrayList<>());
        ChatResponse response = chatService.processChatRequest(request);

        // 验证结果
        assertFalse(response.isSuccess());
        assertNotNull(response.getError());
    }

    @Test
    public void testProcessChatRequest_CharacterNotFound() {
        // 准备测试数据
        String characterId = "999";

        // 模拟依赖行为
        when(characterRepository.getCharacterById(characterId)).thenReturn(null);

        // 执行测试
        ChatRequest request = new ChatRequest(characterId, "你好", new ArrayList<>());
        ChatResponse response = chatService.processChatRequest(request);

        // 验证结果
        assertFalse(response.isSuccess());
        assertEquals("找不到指定的角色", response.getError());
    }

    @Test
    public void testValidateMessageContent() {
        assertTrue(chatService.validateMessageContent("有效的消息"));
        assertFalse(chatService.validateMessageContent(""));
        assertFalse(chatService.validateMessageContent(null));

        // 创建一个超过1000个字符的消息
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1001; i++) {
            longMessage.append("a");
        }
        assertFalse(chatService.validateMessageContent(longMessage.toString()));
    }

    @Test
    public void testFormatMessageTimestamp() {
        Date now = new Date();
        String formatted = chatService.formatMessageTimestamp(now);
        assertNotNull(formatted);
        assertTrue(formatted.length() > 0);
    }
}