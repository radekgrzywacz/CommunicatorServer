package com.communicator.entity.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LastMessageDto {
    private String lastMessage;
    private String lastMessageAuthorId;
    private String lastMessageTime;
    private String lastMessageId;
}
