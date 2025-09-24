package com.tcs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Character {
    private String id;
    private String name;
    private String avatar;
    private String category;
    private String description;
    private String personality;
}