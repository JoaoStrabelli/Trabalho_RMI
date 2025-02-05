package com.edu.utfpr.core.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Chat implements Serializable {
    public UUID chatId;
    public String name;

    public User admin;
    public User created_by;

    public Boolean isGroup;
    public Boolean exitAdminMethodRandom;

    public List<User> members = new ArrayList<>();

    public List<User> pendingUsers = new ArrayList<>();

    public List<Messages> messages = new ArrayList<>();

    public Chat(UUID chatId, String name, User _admin, User created_by, Boolean isGroup,
            Boolean exitAdminMethodRandom) {
        this.chatId = chatId;
        this.name = name;
        this.admin = _admin;
        this.created_by = created_by;
        this.isGroup = isGroup;
        this.exitAdminMethodRandom = exitAdminMethodRandom;

        Messages First = new Messages(created_by, "Utilize o comando /help para listar os comandos possiveis no chat.");

        this.messages.add(First);
        this.members.add(_admin);
    }
}
