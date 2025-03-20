package com.communicator.entity.chat;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *Undelivered message class for safe messages to send when user reconnect to app.
 *
 */
@Data
@Builder
@Document
public class UndeliveredMessage {
    @Id
    private String id;
    private String userId;
    private ChatRoom payload;
    private String type;
}

