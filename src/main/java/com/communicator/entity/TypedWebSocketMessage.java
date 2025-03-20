package com.communicator.entity;

import com.communicator.dto.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TypedWebSocketMessage {
    private MessageType type;
    private Object content;
}
