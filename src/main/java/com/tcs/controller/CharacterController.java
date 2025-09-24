package com.tcs.controller;

import com.tcs.model.Character;
import com.tcs.repository.CharacterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    private final CharacterRepository characterRepository;

    @Autowired
    public CharacterController(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    // 获取所有角色
    @GetMapping
    public ResponseEntity<List<Character>> getAllCharacters() {
        try {
            List<Character> characters = characterRepository.getAllCharacters();
            return ResponseEntity.ok(characters);
        } catch (Exception e) {
            System.err.println("Error fetching characters: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 获取单个角色信息
    @GetMapping("/{id}")
    public ResponseEntity<?> getCharacterById(@PathVariable String id) {
        try {
            if (id == null || id.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("角色ID不能为空");
            }
            
            Character character = characterRepository.getCharacterById(id);
            if (character == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("找不到指定的角色");
            }
            
            return ResponseEntity.ok(character);
        } catch (Exception e) {
            System.err.println("Error fetching character: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取角色信息失败");
        }
    }
}