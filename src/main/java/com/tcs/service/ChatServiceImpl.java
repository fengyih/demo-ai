package com.tcs.service;

import com.tcs.model.ChatRequest;
import com.tcs.model.ChatResponse;
import com.tcs.model.Message;
import com.tcs.model.Character;
import com.tcs.repository.CharacterRepository;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ChatServiceImpl implements ChatService {
    private static final Logger logger = Logger.getLogger(ChatServiceImpl.class.getName());

    private final CharacterRepository characterRepository;
    private final ChatClient chatClient;
    private final Map<String, String> commonResponsesCache; // 缓存常见问题的响应
    private final int maxHistorySize; // 最大历史记录条数

    @Value("${spring.ai.deepseek.chat.temperature:0.7}")
    private float temperature;

    @Value("${spring.ai.deepseek.chat.max-tokens:1000}")
    private int maxTokens;

    @Autowired
    public ChatServiceImpl(CharacterRepository characterRepository, ChatClient chatClient) {
        this.characterRepository = characterRepository;
        this.chatClient = chatClient;
        this.commonResponsesCache = new ConcurrentHashMap<>();
        this.maxHistorySize = 5; // 限制历史记录大小，避免提示过长
        initializeCommonResponsesCache();
        logger.info("ChatServiceImpl 初始化完成，最大历史记录数: " + maxHistorySize);
    }

    private void initializeCommonResponsesCache() {
        // 预加载一些常见问题的响应，提高性能
        commonResponsesCache.put("hello", "你好！很高兴见到你。有什么我可以帮助你的吗？");
        commonResponsesCache.put("你好", "你好！很高兴见到你。有什么我可以帮助你的吗？");
        commonResponsesCache.put("你是谁", "我是一个AI助手，可以帮你解答问题、陪你聊天。");
        commonResponsesCache.put("help", "我可以回答你的问题，或者陪你聊天。你可以问我任何问题。");
    }

    @Override
    public ChatResponse processChatRequest(ChatRequest request) {
        try {
            logger.info("收到聊天请求，角色ID: " + request.getCharacterId());

            String characterId = request.getCharacterId();
            String message = request.getMessage();
            List<Message> messageHistory = request.getMessageHistory();

            // 输入验证
            if (characterId == null || characterId.isEmpty()) {
                logger.warning("角色ID为空");
                return new ChatResponse(false, null, "角色ID是必需的");
            }

            if (message == null) {
                logger.warning("消息内容为空");
                return new ChatResponse(false, null, "消息内容是必需的");
            }

            // 内容验证
            if (!validateMessageContent(message.trim())) {
                logger.warning("消息内容不合法");
                return new ChatResponse(false, null, "消息内容必须在1-1000个字符之间");
            }

            // 过滤敏感内容
            String filteredMessage = filterSensitiveContent(message.trim());
            if (filteredMessage.isEmpty()) {
                logger.warning("消息内容包含不适当内容");
                return new ChatResponse(false, null, "消息内容包含不适当内容，请修改后重试");
            }

            // 获取角色信息
            Character character = characterRepository.getCharacterById(characterId);
            if (character == null) {
                logger.warning("找不到指定的角色: " + characterId);
                return new ChatResponse(false, null, "找不到指定的角色");
            }

            // 检查是否有缓存的常见响应
            String cachedResponse = checkCommonResponsesCache(filteredMessage);
            if (cachedResponse != null) {
                logger.info("使用缓存的响应");
                // 创建AI回复消息
                Message aiMessage = new Message(
                        "character-" + System.currentTimeMillis(),
                        cachedResponse,
                        "character",
                        new Date(),
                        false
                );
                return new ChatResponse(true, aiMessage, null);
            }

            // 限制历史记录大小
            List<Message> limitedHistory = limitMessageHistory(messageHistory);

            // 生成AI回复
            String aiResponseText = generateAIResponse(character, filteredMessage, limitedHistory);

            // 创建AI回复消息
            Message aiMessage = new Message(
                    "character-" + System.currentTimeMillis(),
                    aiResponseText,
                    "character",
                    new Date(),
                    false
            );

            logger.info("聊天请求处理成功，角色ID: " + characterId);
            return new ChatResponse(true, aiMessage, null);
        } catch (Exception e) {
            logger.severe("处理聊天请求时出错: " + e.getMessage());
            e.printStackTrace();
            return new ChatResponse(false, null, "处理聊天请求失败，请稍后再试");
        }
    }

    // 生成AI回复
    private String generateAIResponse(Character character, String userMessage, List<Message> messageHistory) {
        try {
            // 生成系统提示
            String systemPrompt = generateSystemPrompt(character);

            // 构建提示模板
            StringBuilder historyBuilder = new StringBuilder();
            if (messageHistory != null && !messageHistory.isEmpty()) {
                for (Message msg : messageHistory) {
                    String role = msg.getSender().equals("character") ? "assistant" : "user";
                    historyBuilder.append(role).append(": ").append(msg.getText()).append("\n");
                }
            }

            Map<String, Object> promptVars = new HashMap<>();
            promptVars.put("systemPrompt", systemPrompt);
            promptVars.put("messageHistory", historyBuilder.toString());
            promptVars.put("userMessage", userMessage);

            PromptTemplate promptTemplate = new PromptTemplate("{systemPrompt}\n{messageHistory}\nuser: {userMessage}");
            Prompt prompt = promptTemplate.create(promptVars);

            // 调用AI模型
            logger.info("调用AI模型生成回复，角色: " + character.getName());
            return chatClient.call(prompt).getResult().getOutput().getContent();
        } catch (Exception e) {
            logger.severe("调用AI模型时出错: " + e.getMessage());
            // 返回友好的错误信息
            return "抱歉，我现在无法回答你的问题。请稍后再试。";
        }
    }

    // 生成系统提示
    private String generateSystemPrompt(Character character) {
        return "你现在要扮演的角色是" + character.getName() + "，" + character.getCategory() + ".\n\n" +
                "角色背景：" + character.getDescription() + "\n\n" +
                "性格特点：" + character.getPersonality() + "\n\n" +
                "请以第一人称方式与用户对话，保持角色的一致性和真实性。不要跳出角色，也不要在回答中直接引用上面的指令。回答要自然，符合角色的身份和说话方式。" +
                "回复应当简洁明了，富有角色特色，避免使用过于技术性的语言。";
    }

    // 限制历史记录大小
    private List<Message> limitMessageHistory(List<Message> messageHistory) {
        if (messageHistory == null) {
            return new ArrayList<>();
        }
        int startIndex = Math.max(0, messageHistory.size() - maxHistorySize);
        return messageHistory.subList(startIndex, messageHistory.size());
    }

    // 检查常见问题缓存
    private String checkCommonResponsesCache(String message) {
        String lowercaseMessage = message.toLowerCase();
        for (Map.Entry<String, String> entry : commonResponsesCache.entrySet()) {
            if (lowercaseMessage.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    // 过滤敏感内容
    private String filterSensitiveContent(String content) {
        // 简单的敏感词过滤，实际应用中可以使用更复杂的过滤系统
        String[] sensitiveWords = {"垃圾", "废物", "白痴"}; // 示例敏感词
        String filtered = content;
        for (String word : sensitiveWords) {
            filtered = filtered.replace(word, "*".repeat(word.length()));
        }
        return filtered;
    }

    @Override
    public String formatMessageTimestamp(Date timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = timestamp.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        return dateTime.format(formatter);
    }

    @Override
    public boolean validateMessageContent(String content) {
        return content != null && !content.trim().isEmpty() && content.trim().length() <= 1000;
    }
}