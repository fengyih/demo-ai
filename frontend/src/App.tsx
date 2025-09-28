import { useState, useEffect } from 'react';
import axios from 'axios';
import CharactersList from './components/CharactersList';
import ChatInterface from './components/ChatInterface';
import { Character } from './types';
import './App.css';

function App() {
  const [characters, setCharacters] = useState<Character[]>([]);
  const [selectedCharacter, setSelectedCharacter] = useState<Character | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  // 获取所有角色列表
  useEffect(() => {
    const fetchCharacters = async () => {
      try {
        setIsLoading(true);
        const response = await axios.get('/api/characters');
        // 验证返回的数据是否为数组，如果不是则使用模拟数据
        if (Array.isArray(response.data)) {
          setCharacters(response.data);
        } else {
          console.warn('API返回的数据不是数组，使用模拟数据');
          setCharacters(getMockCharacters());
        }
        setError('');
      } catch (err) {
        console.error('获取角色列表失败:', err);
        setError('获取角色列表失败，请稍后再试');
        // 使用模拟数据作为降级方案
        setCharacters(getMockCharacters());
      } finally {
        setIsLoading(false);
      }
    };

    fetchCharacters();
  }, []);

  // 处理角色选择
  const handleCharacterSelect = (character: Character) => {
    setSelectedCharacter(character);
  };

  // 处理返回角色列表
  const handleBackToList = () => {
    setSelectedCharacter(null);
  };

  return (
    <div className="app">
      <header className="app-header">
        <h1>AI角色聊天应用</h1>
      </header>
      <main className="app-main">
        {selectedCharacter ? (
          <ChatInterface 
            character={selectedCharacter} 
            onBack={handleBackToList} 
          />
        ) : (
          <CharactersList 
            characters={characters} 
            onSelectCharacter={handleCharacterSelect} 
            isLoading={isLoading} 
            error={error} 
          />
        )}
      </main>
      <footer className="app-footer">
        <p>&copy; 2025 AI角色聊天应用 - 与历史伟人、文学角色对话</p>
      </footer>
    </div>
  );
}

// 模拟角色数据，用于降级方案
function getMockCharacters(): Character[] {
  return [
    {
      id: 'harry-potter',
      name: '哈利波特',
      avatar: '/avatars/harry-potter.png',
      category: '文学角色',
      description: '哈利波特是J.K.罗琳创作的奇幻小说系列《哈利波特》的主角。他是一个年轻的巫师，在霍格沃茨魔法学校学习魔法，并与黑魔王伏地魔展开了一系列的战斗。',
      personality: '勇敢、忠诚、有责任感、好奇心强。作为格兰芬多学院的学生，他总是愿意为朋友冒险，并且有着强烈的正义感。'
    },
    {
      id: 'socrates',
      name: '苏格拉底',
      avatar: '/avatars/socrates.png',
      category: '历史人物',
      description: '苏格拉底是古希腊著名的哲学家，被认为是西方哲学的奠基人之一。他没有留下任何著作，但他的思想通过他的学生柏拉图的对话录流传下来。',
      personality: '善于提问、追求真理、质疑权威。他使用辩证法（问答法）来探索哲学问题，鼓励人们通过批判性思维来接近真理。'
    },
    {
      id: 'einstein',
      name: '爱因斯坦',
      avatar: '/avatars/einstein.png',
      category: '科学家',
      description: '阿尔伯特·爱因斯坦是20世纪最著名的物理学家，相对论的创始人。他的理论彻底改变了我们对时间、空间、引力和宇宙的理解。',
      personality: '富有创造力、好奇心强、热爱思考、追求简洁。他相信想象力比知识更重要，并且总是试图用简单的理论来解释复杂的自然现象。'
    }
  ];
}

export default App;