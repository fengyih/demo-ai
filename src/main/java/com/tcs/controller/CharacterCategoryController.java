package com.tcs.controller;

import com.tcs.model.Character;
import com.tcs.repository.CharacterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/character-categories")
public class CharacterCategoryController {
    private static final Logger logger = Logger.getLogger(CharacterCategoryController.class.getName());

    private final CharacterRepository characterRepository;

    @Autowired
    public CharacterCategoryController(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
        logger.info("CharacterCategoryController 初始化完成");
    }

    /**
     * 获取所有角色分类
     * @return 分类列表
     */
    @GetMapping
    public ResponseEntity<Set<String>> getAllCategories() {
        try {
            logger.info("获取所有角色分类");
            Set<String> categories = characterRepository.getAllCategories();
            logger.info("成功获取角色分类，数量: " + categories.size());
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.severe("获取角色分类失败: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据分类获取角色列表
     * @param category 角色分类
     * @return 该分类下的角色列表
     */
    @GetMapping("/{category}")
    public ResponseEntity<List<Character>> getCharactersByCategory(@PathVariable String category) {
        try {
            if (category == null || category.trim().isEmpty()) {
                logger.warning("分类参数为空");
                return ResponseEntity.badRequest().build();
            }

            logger.info("获取分类下的角色列表: " + category);
            List<Character> characters = characterRepository.getCharactersByCategory(category);
            logger.info("成功获取分类下的角色，数量: " + characters.size());
            return ResponseEntity.ok(characters);
        } catch (Exception e) {
            logger.severe("获取分类下的角色列表失败: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}