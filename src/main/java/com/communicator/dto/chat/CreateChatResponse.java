package com.communicator.dto.chat;

import com.communicator.entity.chat.ChatRoom;

public record CreateChatResponse(
    ChatRoom chatRoom
) {
}
