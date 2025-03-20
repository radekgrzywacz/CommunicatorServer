package com.communicator.dto.chat;

import com.communicator.entity.chat.ChatRoom;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

/**
 * DTO representing a page of ChatRooms.
 *
 * This class encapsulates a paginated list of {@link ChatRoom} entities along with a
 * message providing additional context or information about the page of results.
 * It is typically used to transfer a paginated set of chat room data between layers
 * of the application, such as from the service to the controller.
 *
 */
@Getter
@Setter
public class ChatRoomPageDto {
    /**
     * A {@link Page} of {@link ChatRoom} entities representing the current page of results.
     */
    private Page<ChatRoom> results;

    /**
     * A message providing additional context or information about the page of results.
     * It could be used for status or error messaging.
     */
    private String message;
}
