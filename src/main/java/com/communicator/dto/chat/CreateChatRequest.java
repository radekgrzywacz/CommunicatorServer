package com.communicator.dto.chat;

public record CreateChatRequest(
        String[] usersIds
) {
}
