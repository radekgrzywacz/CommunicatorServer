package com.communicator.dto.chat;

import com.communicator.dto.UserDto;

import java.time.Instant;
import java.util.List;

public record ChatPreview(
        String chatId,
        List<UserDto> users,
        boolean active,
        String photo,
        String lastMessageContent,
        String lastMessageAuthorId,
        Instant lastMessageTime,
        String lastMessageId
) {
}
