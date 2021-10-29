package ru.kpfu.itis.models;

import com.fasterxml.jackson.annotation.*;

import java.util.List;

public class Messages {
    private List<Message> messages;

    @JsonProperty("messages")
    public List<Message> getMessages() { return messages; }
    @JsonProperty("messages")
    public void setMessages(List<Message> value) { this.messages = value; }
}