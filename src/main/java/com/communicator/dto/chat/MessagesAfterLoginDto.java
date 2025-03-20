package com.communicator.dto.chat;

import com.communicator.entity.chat.UndeliveredMessage;

import java.util.List;
import java.util.Map;

public record MessagesAfterLoginDto(
        List<ChatPreview> chats,
        List<UndeliveredMessage> undeliveredMessages,
        Map<String, Boolean> friendsActivity
) {
}
