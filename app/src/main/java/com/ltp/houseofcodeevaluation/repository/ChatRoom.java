package com.ltp.houseofcodeevaluation.repository;

import java.util.List;

public class ChatRoom {
    private String name;
    private String description;

    public ChatRoom() {
    }

    public ChatRoom(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
