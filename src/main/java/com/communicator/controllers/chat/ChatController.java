package com.communicator.controllers.chat;


import com.communicator.dto.MessageType;
import com.communicator.dto.UserDto;
import com.communicator.dto.chat.ChatRoomPageDto;
import com.communicator.entity.TypedWebSocketMessage;
import com.communicator.entity.chat.ChatMessage;
import com.communicator.dto.chat.CreateChatRequest;
import com.communicator.dto.chat.CreateChatResponse;
import com.communicator.entity.chat.ChatRoom;
import com.communicator.repositories.ChatMessageRepository;
import com.communicator.repositories.ChatRepository;
import com.communicator.services.chat.ChatMessageService;
import com.communicator.services.chat.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller handling chat-related WebSocket messages.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    private final ChatMessageRepository chatMessageRepository;

    private final ChatService chatService;
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRepository chatRepository;

    /**
     * Handles sending chat messages to the public topic.
     *
     * @param chatMessage the message payload
     */
    @MessageMapping("/chat/message")
    public void sendMessage(@Payload final ChatMessage chatMessage) {

        String chatId = chatMessage.getChatId();
        ChatRoom chatRoom = chatService.getChat(chatId);
        List<UserDto> users = chatRoom.getUsers();
        chatMessageService.saveChatMessage(chatMessage);
        chatRoom.setLastMessage(chatMessage);
        chatService.saveChat(chatRoom);
        TypedWebSocketMessage message = new TypedWebSocketMessage(MessageType.CHAT_MESSAGE, chatMessage);
        if (log.isInfoEnabled()) {
            log.info("Message sent: {}", message);
            log.info(users.toString());
        }

        for (UserDto user : users) {
            String userId = user.getUserId();

            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/" + userId,
                    message
            );
        }
    }

    /**
     * asd.
     * @param chatId asdj
     * @return asd
     */
    @GetMapping("/getusers/{chatId}")
    public ResponseEntity<List<UserDto>> getUsers(final @PathVariable String chatId) {
        List<UserDto> users = chatService.findUsersFromChatById(chatId);
        return ResponseEntity.ok(users);
    }

    /**
     * Method that creates chat room.
     *
     * @param createChatRequest - request with list of users participating in the chat
     * @return - response entity with new chat
     */
    @PostMapping
    @RequestMapping("/chats")
    public ResponseEntity<CreateChatResponse> createChatRoom(@RequestBody final CreateChatRequest createChatRequest) {
        return chatService.createChat(createChatRequest);
    }

    /**
     * Endpoint for retrieving chat rooms based on a search query.
     *
     * @param query the search query to filter chat rooms.
     * @param page the page number for pagination, default is 0.
     * @param size the size of each page, default is 10.
     * @return a ResponseEntity containing a ChatRoomPageDto object:
     *         - If chat rooms matching the query are found, it includes the results.
     *         - If no chat rooms are found, it returns a "Not Found" status with a message.
     */
    @GetMapping("/rooms")
    public ResponseEntity<ChatRoomPageDto> getChatRooms(final @RequestParam String query,
            final @RequestParam(defaultValue = "1") int page,
            final @RequestParam(defaultValue = "10") int size) {


        Page<ChatRoom> foundRooms = chatService.getChatRooms(query, page, size);
        if (foundRooms.getTotalElements() > 0) {
            ChatRoomPageDto chatRoomPageDto = new ChatRoomPageDto();
            chatRoomPageDto.setResults(foundRooms);
            return ResponseEntity.ok(chatRoomPageDto);
        }

        ChatRoomPageDto chatRoomPageDto = new ChatRoomPageDto();
        chatRoomPageDto.setMessage("No chat rooms found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(chatRoomPageDto);
    }

    /**
     *
     * @param chatId id of chat
     * @param messageId id of message
     * @param page the page number for pagination, default is 0.
     * @param size the size of each page, default is 10.
     * @return a ResponseEntity containing a message
     */
    @GetMapping("/messages/{chatId}/before/{messageId}")
    public ResponseEntity<Page<ChatMessage>> getOlderMessages(
            @PathVariable final String chatId,
            @PathVariable final String messageId,
            @RequestParam(defaultValue = "1") final int page,
            @RequestParam(defaultValue = "10") final int size) {

        Page<ChatMessage> messages = chatMessageService.getChatMessagesBefore(chatId, messageId, page, size);
        return ResponseEntity.ok(messages);
    }
}
