package com.communicator.dto.chat;

import com.communicator.entity.chat.ChatMessage;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
public class ChatMessagePageDto {
    /**
     * A {@link Page} of {@link ChatMessage} entities representing the current page of results.
     */
    private Page<ChatMessage> results;

    /**
     * A message providing additional context or information about the page of results.
     * It could be used for status or error messaging.
     */
    private String message;
}
