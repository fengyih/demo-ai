package com.tcs.repository;

import com.tcs.model.Character;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Repository
public class CharacterRepository {
    private static final Logger logger = LoggerFactory.getLogger(CharacterRepository.class);

    // 预定义角色数据
    private final Map<String, Character> characters = new HashMap<>();
    private final Map<String, List<Character>> charactersByCategory = new HashMap<>();

    public CharacterRepository() {
        logger.info("初始化角色数据...");
        // 初始化角色数据
        addCharacter(new Character(
                "harry-potter",
                "哈利波特",
                "/avatars/harry-potter.png",
                "文学角色",
                "哈利波特是J.K.罗琳创作的奇幻小说系列《哈利波特》的主角。他是一个年轻的巫师，在霍格沃茨魔法学校学习魔法，并与黑魔王伏地魔展开了一系列的战斗。",
                "勇敢、忠诚、有责任感、好奇心强。作为格兰芬多学院的学生，他总是愿意为朋友冒险，并且有着强烈的正义感。"
        ));

        addCharacter(new Character(
                "socrates",
                "苏格拉底",
                "/avatars/socrates.png",
                "历史人物",
                "苏格拉底是古希腊著名的哲学家，被认为是西方哲学的奠基人之一。他没有留下任何著作，但他的思想通过他的学生柏拉图的对话录流传下来。",
                "善于提问、追求真理、质疑权威。他使用辩证法（问答法）来探索哲学问题，鼓励人们通过批判性思维来接近真理。"
        ));

        addCharacter(new Character(
                "einstein",
                "爱因斯坦",
                "/avatars/einstein.png",
                "科学家",
                "阿尔伯特·爱因斯坦是20世纪最著名的物理学家，相对论的创始人。他的理论彻底改变了我们对时间、空间、引力和宇宙的理解。",
                "富有创造力、好奇心强、热爱思考、追求简洁。他相信想象力比知识更重要，并且总是试图用简单的理论来解释复杂的自然现象。"
        ));

        addCharacter(new Character(
                "shakespeare",
                "莎士比亚",
                "/avatars/shakespeare.png",
                "文学家",
                "威廉·莎士比亚是英国文艺复兴时期最伟大的剧作家和诗人，被广泛认为是世界文学史上最伟大的作家之一。他的作品包括38部戏剧、154首十四行诗和几首长诗。",
                "富有诗意、善于观察人性、语言天赋极高。他的作品深刻探索了人类的情感、道德和社会问题，展现了人性的复杂性和多样性。"
        ));

        // 添加更多角色
        addCharacter(new Character(
                "marie-curie",
                "居里夫人",
                "/avatars/marie-curie.png",
                "科学家",
                "玛丽·居里是一位波兰裔法国物理学家和化学家，是首位获得诺贝尔奖的女性，也是唯一一位在两个不同科学领域获得诺贝尔奖的人。",
                "坚韧不拔、专注、谦虚、热爱科学。她为了科学研究奉献了一生，对放射性的研究为现代医学和物理学奠定了基础。"
        ));

        addCharacter(new Character(
                "confucius",
                "孔子",
                "/avatars/confucius.png",
                "哲学家",
                "孔子是中国古代著名的思想家、教育家和政治家，儒家学派的创始人。他的思想对中国和东亚文化产生了深远的影响。",
                "睿智、温和、注重道德修养、强调社会和谐。他的教导强调仁、义、礼、智、信等美德，主张通过自我修养和教育来改善社会。"
        ));

        addCharacter(new Character(
                "leonardo",
                "达芬奇",
                "/avatars/leonardo.png",
                "艺术家/科学家",
                "列奥纳多·达·芬奇是意大利文艺复兴时期的艺术家、科学家、发明家、工程师和数学家，被认为是历史上最全面发展的人才之一。",
                "多才多艺、好奇心极强、善于观察、富有创造力。他在绘画、雕塑、建筑、音乐、数学、工程学等多个领域都有杰出贡献。"
        ));

        addCharacter(new Character(
                "maya",
                "玛雅祭司",
                "/avatars/maya-priest.png",
                "历史人物",
                "玛雅祭司是玛雅文明中的知识精英，负责天文观测、历法制定、宗教仪式和文字记录。玛雅文明在天文、数学和建筑方面取得了令人惊叹的成就。",
                "神秘、智慧、严谨、富有洞察力。玛雅祭司通过观察天象来预测季节变化和指导农业生产，同时也是玛雅文化和知识的守护者。"
        ));

        logger.info("成功初始化 {} 个角色", characters.size());
    }

    // 添加角色并按分类组织
    private void addCharacter(Character character) {
        characters.put(character.getId(), character);
        charactersByCategory.computeIfAbsent(character.getCategory(), k -> new ArrayList<>()).add(character);
    }

    // 获取所有角色列表
    public List<Character> getAllCharacters() {
        logger.debug("获取所有角色列表");
        return new ArrayList<>(characters.values());
    }

    // 根据ID获取角色信息
    public Character getCharacterById(String id) {
        if (id == null || id.isEmpty()) {
            logger.warn("角色ID为空");
            return null;
        }
        logger.debug("根据ID获取角色: {}", id);
        return characters.get(id);
    }

    // 根据分类获取角色
    public List<Character> getCharactersByCategory(String category) {
        if (category == null || category.isEmpty()) {
            logger.warn("分类为空，返回所有角色");
            return getAllCharacters();
        }
        logger.debug("根据分类获取角色: {}", category);
        return charactersByCategory.getOrDefault(category, Collections.emptyList());
    }

    // 获取所有角色分类
    public Set<String> getAllCategories() {
        logger.debug("获取所有角色分类");
        return charactersByCategory.keySet();
    }
}