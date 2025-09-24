package com.tcs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String id;
    private String text;
    private String sender; // 'user' or 'character'
    private Date timestamp;
    private boolean voice = false;
}