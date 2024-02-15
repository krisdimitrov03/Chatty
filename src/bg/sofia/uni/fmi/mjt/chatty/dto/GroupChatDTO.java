package bg.sofia.uni.fmi.mjt.chatty.dto;

import bg.sofia.uni.fmi.mjt.chatty.server.model.Message;

import java.util.SequencedCollection;

public record GroupChatDTO(String name, String[] users, SequencedCollection<Message> messages) {
}
