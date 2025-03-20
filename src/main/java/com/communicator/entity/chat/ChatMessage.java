package com.communicator.entity.chat;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

//import java.awt.*;
/**
 * Represents a chat message with its content, sender, and type.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class ChatMessage {

    @Id
    private String messageId;
    private String content;
    private String chatId;
    private String senderId;
    private Instant timestamp;

    /**
     * Changing message to string.
     *
     * @return message as string
     */
    @Override
    public String toString() {
        return "ChatMessage{"
                + "messageId='" + messageId + '\''
                + ", content='" + content + '\''
                + ", chatId='" + chatId + '\''
                + ", timestamp='" + timestamp + '\''
                + '}';
    }
}
