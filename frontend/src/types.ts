// 角色数据模型
export interface Character {
  id: string;
  name: string;
  avatar: string;
  category: string;
  description: string;
  personality: string;
}

// 消息数据模型
export interface Message {
  id: string;
  text: string;
  sender: 'user' | 'character';
  timestamp: Date;
  voice?: boolean;
}

// 聊天请求模型
export interface ChatRequest {
  characterId: string;
  message?: string;
  audioData?: Uint8Array;
  messageHistory: Message[];
}

// 聊天响应模型
export interface ChatResponse {
  success: boolean;
  message?: Message;
  error?: string;
}