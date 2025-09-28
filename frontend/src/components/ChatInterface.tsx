import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import { Character, Message, ChatRequest, ChatResponse } from '../types';
import './ChatInterface.css';

interface ChatInterfaceProps {
  character: Character;
  onBack: () => void;
}

const ChatInterface: React.FC<ChatInterfaceProps> = ({ character, onBack }) => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputMessage, setInputMessage] = useState('');
  const [isSending, setIsSending] = useState(false);
  const [error, setError] = useState('');
  const chatContainerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  // 自动滚动到底部
  useEffect(() => {
    if (chatContainerRef.current) {
      chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
    }
  }, [messages]);

  // 当角色改变时，重置聊天历史
  useEffect(() => {
    setMessages([]);
    setError('');
    // 自动聚焦到输入框
    if (inputRef.current) {
      inputRef.current.focus();
    }
  }, [character]);

  // 发送消息
  const sendMessage = async () => {
    if (!inputMessage.trim() || isSending) {
      return;
    }

    const userMessage: Message = {
      id: `msg-${Date.now()}`,
      text: inputMessage.trim(),
      sender: 'user',
      timestamp: new Date()
    };

    // 添加用户消息到消息列表
    setMessages(prevMessages => [...prevMessages, userMessage]);
    setInputMessage('');
    setIsSending(true);
    setError('');

    try {
      // 构建聊天请求
      const chatRequest: ChatRequest = {
        characterId: character.id,
        message: userMessage.text,
        messageHistory: [...messages, userMessage]
      };

      // 发送请求到后端
      const response = await axios.post<ChatResponse>('/api/chat', chatRequest);

      if (response.data.success && response.data.message) {
        // 添加角色回复到消息列表
        setMessages(prevMessages => [...prevMessages, response.data.message as Message]);
      } else {
        setError(response.data.error || '获取回复失败');
      }
    } catch (err) {
      console.error('发送消息失败:', err);
      setError('网络错误，请稍后再试');
      // 添加模拟回复作为降级方案
      const mockReply: Message = {
        id: `msg-${Date.now()}-mock`,
        text: `我是${character.name}。由于网络问题，这是一条模拟回复。请稍后再试，体验完整的AI对话功能。`,
        sender: 'character',
        timestamp: new Date()
      };
      setMessages(prevMessages => [...prevMessages, mockReply]);
    } finally {
      setIsSending(false);
    }
  };

  // 处理回车发送
  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  // 格式化时间
  const formatTime = (date: Date): string => {
    return date.toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  };

  return (
    <div className="chat-interface">
      {/* 聊天头部 */}
      <div className="chat-header">
        <button className="back-button" onClick={onBack}>
          ← 返回角色列表
        </button>
        <div className="character-info-header">
          <div className="character-avatar-small">
            {character.name.charAt(0)}
          </div>
          <div>
            <h2>{character.name}</h2>
            <p>{character.category}</p>
          </div>
        </div>
      </div>

      {/* 角色描述 */}
      <div className="character-description-section">
        <h3>角色介绍</h3>
        <p>{character.description}</p>
        <h4>性格特点</h4>
        <p>{character.personality}</p>
      </div>

      {/* 聊天内容区域 */}
      <div className="chat-container" ref={chatContainerRef}>
        {messages.length === 0 ? (
          <div className="chat-empty">
            <p>开始与 {character.name} 对话吧！</p>
          </div>
        ) : (
          messages.map(message => (
            <div 
              key={message.id} 
              className={`message ${message.sender}`}
            >
              <div className="message-content">
                <p className="message-text">{message.text}</p>
                <span className="message-time">{formatTime(message.timestamp)}</span>
              </div>
              {message.sender === 'character' && (
                <div className="character-avatar-small">
                  {character.name.charAt(0)}
                </div>
              )}
            </div>
          ))
        )}
        {isSending && (
          <div className="message character sending">
            <div className="message-content">
              <div className="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
            <div className="character-avatar-small">
              {character.name.charAt(0)}
            </div>
          </div>
        )}
      </div>

      {/* 错误提示 */}
      {error && <div className="error">{error}</div>}

      {/* 输入区域 */}
      <div className="chat-input-area">
        <input
          ref={inputRef}
          type="text"
          value={inputMessage}
          onChange={(e) => setInputMessage(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder={`与 ${character.name} 说点什么...`}
          className="chat-input"
          disabled={isSending}
        />
        <button 
          onClick={sendMessage} 
          className="send-button"
          disabled={isSending || !inputMessage.trim()}
        >
          {isSending ? '发送中...' : '发送'}
        </button>
      </div>
    </div>
  );
};

export default ChatInterface;