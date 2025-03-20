package com.communicator.services.chat;

import com.communicator.entity.TypedWebSocketMessage;
import com.communicator.entity.chat.ActivityStatusUpdate;
import com.communicator.dto.MessageType;
import com.communicator.dto.UserDto;
import com.communicator.entity.AppUser;
import com.communicator.entity.chat.ChatRoom;
import com.communicator.entity.chat.UndeliveredMessage;
import com.communicator.repositories.ChatRepository;
import com.communicator.repositories.UndeliveredMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatBroadcastService {
    private final SimpMessagingTemplate messagingTemplate;
    private final UndeliveredMessageRepository undeliveredMessageRepository;
    private final ChatRepository chatRepository;

    /**
     * Method to broadcast info about being in new chat.
     *
     * @param chatRoom - created chat room
     * @param users - list of users in chat room
     */
    public void broadcastNewChat(final ChatRoom chatRoom, final List<AppUser> users) {
        if (log.isInfoEnabled()) {
            log.warn("In broadcastNewChat");
        }
        TypedWebSocketMessage message = new TypedWebSocketMessage(MessageType.NEW_CHAT, chatRoom);
        users.forEach(user -> {
            if (user.isActive()) {
                messagingTemplate.convertAndSendToUser(
                        user.getPhoneNumber(),
                        "/" + user.getPhoneNumber(),
                        message
                );
                if (log.isInfoEnabled()) {
                    log.warn("Message send to chat room: {}", chatRoom);
                }

            } else {
                saveUndeliveredMessage(user.getPhoneNumber(), chatRoom);
                if (log.isInfoEnabled()) {
                    log.warn("In else");
                }
            }
        });
    }

    /**
     * Broadcasting activity status change.
     * @param userId user id
     * @param isActive user activity status
     */
    public void broadcastStatusToChatPartners(final String userId, final boolean isActive) {
        // Find all the chat rooms the user is part of
        List<ChatRoom> chatRooms = chatRepository.findByUserId(userId);

        TypedWebSocketMessage webSocketMessage = new TypedWebSocketMessage(MessageType.ACTIVITY_STATUS_UPDATE,
                new ActivityStatusUpdate(userId, isActive));

        Set<String> uniqueUserIds = getFriendsIds(userId, chatRooms);

        // Now send the activity status update to each unique user
        for (String recipientUserId : uniqueUserIds) {
            messagingTemplate.convertAndSendToUser(
                    recipientUserId,
                    "/" + recipientUserId, // Or any other topic
                    webSocketMessage
            );
        }
    }

    /**
     * Method to get ids of all friended users.
     * @param userId current user id
     * @param usersChatRooms current user chatrooms
     * @return Set of ids
     */
    public Set<String> getFriendsIds(final String userId, final List<ChatRoom> usersChatRooms) {
        // Use a Set to collect unique users (to avoid duplicates)
        Set<String> uniqueUserIds = new HashSet<>();

        // Loop through chat rooms and collect the other users (exclude the current user)
        for (ChatRoom chatRoom : usersChatRooms) {
            List<UserDto> usersInChat = chatRoom.getUsers().stream()
                .filter(user -> !user.getUserId().equals(userId))  // Exclude the sender
                .collect(Collectors.toList());

            // Add each user to the set (duplicates will be automatically avoided)
            for (UserDto user : usersInChat) {
                uniqueUserIds.add(user.getUserId());
            }
        }
        return uniqueUserIds;
    }


    private void saveUndeliveredMessage(final String userId, final ChatRoom chatDto) {
        UndeliveredMessage undeliveredMessage = UndeliveredMessage.builder()
                .userId(userId)
                .payload(chatDto)
                .type("NEW_CHAT")
                .build();
        undeliveredMessageRepository.save(undeliveredMessage);
        if (log.isInfoEnabled()) {
            log.warn("Message saved to chat room: {}", undeliveredMessage);
        }
    }
}

