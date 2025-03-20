package com.communicator.config.websockets;

import com.communicator.dto.MessageType;
import com.communicator.dto.chat.ChatPreview;
import com.communicator.dto.chat.MessagesAfterLoginDto;
import com.communicator.entity.AppUser;
import com.communicator.entity.TypedWebSocketMessage;
import com.communicator.entity.chat.ChatMessage;
import com.communicator.entity.chat.ChatRoom;
import com.communicator.entity.chat.UndeliveredMessage;
import com.communicator.repositories.ChatMessageRepository;
import com.communicator.repositories.ChatRepository;
import com.communicator.repositories.UndeliveredMessageRepository;
import com.communicator.repositories.UserRepository;
import com.communicator.services.UserService;
import com.communicator.services.chat.ChatBroadcastService;
import com.communicator.services.chat.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens to WebSocket events such as user disconnections.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Controller
public class WebSocketEventListener {

    /**
     * Service to send messages to subscribed clients.
     */
    private final SimpMessageSendingOperations messagingTemplate;
    private final UserRepository userRepository;
    private final UndeliveredMessageRepository undeliveredMessageRepository;
    private final ConcurrentHashMap<String, String> sessionRegistry = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> subscriptionAcknowledged = new ConcurrentHashMap<>();
    private final ChatRepository chatRepository;
    private final ChatService chatService;
    private final UserService userService;
    private final ChatBroadcastService chatBroadcastService;
    private final ChatMessageRepository chatMessageRepository;




    /**
     * Method that listens for websocket events from client.
     * @param event event from client
     */
    @EventListener
    public void handleWebSocketConnectListener(final SessionConnectedEvent event) {
        var headers = event.getMessage().getHeaders();
        String phoneNumber = getPhoneNumberFromHeaders(headers.toString());
        String sessionId = headers.get("simpSessionId").toString();

        if (phoneNumber != null) {
            sessionRegistry.put(sessionId, phoneNumber);

            AppUser appUser = userService.findByPhoneNumber(phoneNumber);
            appUser.setActive(true);
            userRepository.save(appUser);
            chatBroadcastService.broadcastStatusToChatPartners(appUser.getPhoneNumber(), appUser.isActive());
        } else {
            log.error("Failed to extract phone number from headers");
        }
    }

    /**
     * Get acknowledgment from client.
     * @param payload payload from client
     */
    @MessageMapping("/ack")
    public void handleAcknowledgment(final Map<String, String> payload) {

        String userId = payload.get("userId");
        if (userId != null) {
            subscriptionAcknowledged.put(userId, true);

            processMessagesForClient(userId);
        } else {
            log.error("Acknowledgment received with null userId");
        }
    }

    /**
     * Get last messages.
     * @param payload payload from client
     */
    @MessageMapping("/lastMessage")
    public void lastMessage(final Map<String, String> payload) {
        String chatId = payload.get("chatId");
        String userId = payload.get("userId");
        if (chatId != null && userId != null) {
            List<ChatMessage> messages = chatMessageRepository.findTop20ByChatIdOrderByTimestampDesc(chatId);
            TypedWebSocketMessage webSocketLastMessages = new TypedWebSocketMessage(MessageType.LAST_MESSAGES,
                    messages);
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/" + userId,
                    webSocketLastMessages
            );
        } else {
            log.error("Invalid payload for lastMessage: {}", payload);
        }
    }


    private void processMessagesForClient(final String userId) {
        if (subscriptionAcknowledged.getOrDefault(userId, false)) {
            List<UndeliveredMessage> undeliveredMessages = undeliveredMessageRepository.findByUserId(userId);
            List<ChatRoom> chatRooms = chatRepository.findTop10ByUserId(userId);
            List<ChatPreview> chatPreviews = chatService.toChatPreviewList(chatRooms);
            Map<String, Boolean> friendsActivity = getFriendsActivity(userId);

            MessagesAfterLoginDto message = new MessagesAfterLoginDto(chatPreviews, undeliveredMessages,
                friendsActivity);
            TypedWebSocketMessage webSocketMessage = new TypedWebSocketMessage(MessageType.ALL_CHATS, message);

            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/" + userId,
                    webSocketMessage
            );

            subscriptionAcknowledged.remove(userId);
        }
    }

    private Map<String, Boolean> getFriendsActivity(final String userId) {
        List<ChatRoom> allUserChatRooms = chatRepository.findByUserId(userId);
        Set<String> friendsIds = chatBroadcastService.getFriendsIds(userId, allUserChatRooms);
        List<AppUser> users = userRepository.findByPhoneNumberIn(friendsIds);
        Map<String, Boolean> friendsActivity = new HashMap<>();
        for (AppUser user : users) {
            friendsActivity.put(user.getPhoneNumber(), user.isActive());
        }

        return friendsActivity;
    }

    /**
     * Handles WebSocket disconnection events. Sends a "user left" message to all clients.
     *
     * @param event the disconnection event.
     */
    @EventListener
    public void handleWebSocketDisconnectListener(final SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        log.info("WebSocket disconnect event: sessionId={}", sessionId);

        // Retrieve phone number from sessionRegistry
        String phoneNumber = sessionRegistry.get(sessionId);
        if (phoneNumber == null) {
            return; // Exit gracefully
        }

        AppUser appUser = userService.findByPhoneNumber(phoneNumber);
        if (appUser != null) {
            appUser.setActive(false);
            userRepository.save(appUser);
            chatBroadcastService.broadcastStatusToChatPartners(appUser.getPhoneNumber(), appUser.isActive());
            log.info("User set to inactive: {}", phoneNumber);
        } else {
            log.warn("User not found for phone number: {}", phoneNumber);
        }

        sessionRegistry.remove(sessionId);
        log.info("Session removed: sessionId={}, phoneNumber={}", sessionId, phoneNumber);
    }

    private String getPhoneNumberFromHeaders(final String headers) {
        if (headers == null || headers.isEmpty()) {
            log.error("Headers are null or empty");
            return null;
        }

        // Look for the "phoneNumber=" part in the headers
        String key = "phoneNumber=[";
        int startIndex = headers.indexOf(key);
        if (startIndex == -1) {
            log.error("Phone number not found in headers: {}", headers);
            return null;
        }

        // Extract everything after "phoneNumber=[" and before the closing "]"
        startIndex += key.length();
        int endIndex = headers.indexOf("]", startIndex);
        if (endIndex == -1) {
            log.error("Invalid phone number format in headers: {}", headers);
            return null;
        }

        // Extract and trim the phone number
        String phoneNumber = headers.substring(startIndex, endIndex).trim();
        return phoneNumber.isEmpty() ? null : phoneNumber;
    }
}
