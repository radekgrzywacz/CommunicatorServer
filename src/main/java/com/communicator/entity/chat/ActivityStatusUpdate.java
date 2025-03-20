package com.communicator.entity.chat;

public record ActivityStatusUpdate(
        String userId,
        boolean active
) {
}
