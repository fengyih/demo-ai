import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Character } from '../types';
import './CharactersList.css';

interface CharactersListProps {
  characters: Character[];
  onSelectCharacter: (character: Character) => void;
  isLoading: boolean;
  error: string;
}

const CharactersList: React.FC<CharactersListProps> = ({
  characters,
  onSelectCharacter,
  isLoading,
  error
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [searchType, setSearchType] = useState<'name' | 'keyword'>('name');
  const [filteredCharacters, setFilteredCharacters] = useState<Character[]>([]);
  const [searchLoading, setSearchLoading] = useState(false);
  const [categories, setCategories] = useState<string[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>('all');

  // 初始化筛选后的角色列表和分类
  useEffect(() => {
    setFilteredCharacters(characters);
    
    // 提取所有分类
    const uniqueCategories = Array.from(new Set(characters.map(character => character.category)));
    setCategories(uniqueCategories);
  }, [characters]);

  // 处理搜索
  const handleSearch = async () => {
    if (!searchTerm.trim()) {
      setFilteredCharacters(characters);
      setSelectedCategory('all');
      return;
    }

    try {
      setSearchLoading(true);
      const endpoint = searchType === 'name' 
        ? `/api/characters/search/name?name=${encodeURIComponent(searchTerm.trim())}`
        : `/api/characters/search/keyword?keyword=${encodeURIComponent(searchTerm.trim())}`;
      
      const response = await axios.get(endpoint);
      setFilteredCharacters(response.data);
      setSelectedCategory('all');
    } catch (err) {
      console.error('搜索角色失败:', err);
      // 降级方案：在本地进行搜索
      const results = searchType === 'name' 
        ? searchCharactersByName(searchTerm.trim())
        : searchCharactersByKeyword(searchTerm.trim());
      setFilteredCharacters(results);
    } finally {
      setSearchLoading(false);
    }
  };

  // 本地搜索 - 按名称
  const searchCharactersByName = (name: string): Character[] => {
    const lowercaseName = name.toLowerCase();
    return characters.filter(character => 
      character.name.toLowerCase().includes(lowercaseName)
    );
  };

  // 本地搜索 - 按关键词
  const searchCharactersByKeyword = (keyword: string): Character[] => {
    const lowercaseKeyword = keyword.toLowerCase();
    return characters.filter(character => 
      character.name.toLowerCase().includes(lowercaseKeyword) ||
      character.description.toLowerCase().includes(lowercaseKeyword) ||
      character.personality.toLowerCase().includes(lowercaseKeyword) ||
      character.category.toLowerCase().includes(lowercaseKeyword)
    );
  };

  // 处理分类筛选
  const handleCategoryFilter = (category: string) => {
    setSelectedCategory(category);
    if (category === 'all') {
      setFilteredCharacters(characters);
    } else {
      const filtered = characters.filter(character => character.category === category);
      setFilteredCharacters(filtered);
    }
  };

  // 处理回车搜索
  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  return (
    <div className="characters-list">
      <h2>选择一个角色开始对话</h2>
      
      {/* 搜索区域 */}
      <div className="search-section">
        <div className="search-input-group">
          <input
            type="text"
            placeholder="搜索角色..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            onKeyPress={handleKeyPress}
            className="search-input"
          />
          <select
            value={searchType}
            onChange={(e) => setSearchType(e.target.value as 'name' | 'keyword')}
            className="search-type-select"
          >
            <option value="name">按名称搜索</option>
            <option value="keyword">按关键词搜索</option>
          </select>
          <button 
            onClick={handleSearch} 
            className="search-button"
            disabled={searchLoading}
          >
            {searchLoading ? '搜索中...' : '搜索'}
          </button>
        </div>
        
        {/* 分类筛选 */}
        <div className="category-filter">
          <button
            className={`category-button ${selectedCategory === 'all' ? 'active' : ''}`}
            onClick={() => handleCategoryFilter('all')}
          >
            全部
          </button>
          {categories.map(category => (
            <button
              key={category}
              className={`category-button ${selectedCategory === category ? 'active' : ''}`}
              onClick={() => handleCategoryFilter(category)}
            >
              {category}
            </button>
          ))}
        </div>
      </div>

      {/* 错误提示 */}
      {error && <div className="error">{error}</div>}

      {/* 加载状态 */}
      {isLoading && (
        <div className="loading">
          <span>正在加载角色列表...</span>
        </div>
      )}

      {/* 角色列表 */}
      {!isLoading && (
        <div className="characters-grid">
          {filteredCharacters.length > 0 ? (
            filteredCharacters.map(character => (
              <CharacterCard
                key={character.id}
                character={character}
                onSelect={() => onSelectCharacter(character)}
              />
            ))
          ) : (
            <div className="no-results">
              <p>没有找到匹配的角色</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

// 角色卡片组件
interface CharacterCardProps {
  character: Character;
  onSelect: () => void;
}

const CharacterCard: React.FC<CharacterCardProps> = ({ character, onSelect }) => {
  return (
    <div className="character-card" onClick={onSelect}>
      <div className="character-avatar">
        {/* 使用图片占位符，因为实际图片可能不存在 */}
        <div className="avatar-placeholder">
          {character.name.charAt(0)}
        </div>
      </div>
      <div className="character-info">
        <h3 className="character-name">{character.name}</h3>
        <p className="character-category">{character.category}</p>
        <p className="character-description">
          {character.description.length > 80 
            ? `${character.description.substring(0, 80)}...` 
            : character.description}
        </p>
      </div>
    </div>
  );
};

export default CharactersList;