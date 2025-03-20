package com.communicator.services.chat;

import com.communicator.dto.chat.ChatPreview;
import com.communicator.dto.chat.CreateChatRequest;
import com.communicator.dto.chat.CreateChatResponse;
import com.communicator.entity.AppUser;
import com.communicator.entity.chat.ChatRoom;
import com.communicator.repositories.ChatRepository;
import com.communicator.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.communicator.dto.UserDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatBroadcastService chatBroadcastService;

    /**
     * Method that creates new chat.
     *
     * @param request - request with users ids
     * @return - new response entity
     */
    public ResponseEntity<CreateChatResponse> createChat(final CreateChatRequest request) {
        List<String> userIds = Arrays.asList(request.usersIds());
        log.info("Creating chat with users: {}", userIds);

        if (userIds.size() == 2) {
            if (log.isInfoEnabled()) {
                log.info("in chat check");
            }
            List<ChatRoom> existingChats = chatRepository.findByExactUsersPhoneNumber(userIds);
            if (log.isInfoEnabled()) {
                log.info("Found existing chats: {}", existingChats);
            }
            if (!existingChats.isEmpty()) {
                return ResponseEntity.status(405).body(null);
            }
        }

        List<AppUser> users = userRepository.findByPhoneNumberIn(userIds);
        if (log.isInfoEnabled()) {
            log.info("Found {} users", users.size());
        }
        for (AppUser user : users) {
            if (log.isInfoEnabled()) {
                log.info(user.toString());
            }
        }

        ChatRoom chatRoom = new ChatRoom();
        List<UserDto> usersDtos = mapUsersToDTO(users);
        chatRoom.setUsers(usersDtos);

        ChatRoom result = chatRepository.save(chatRoom);
        chatBroadcastService.broadcastNewChat(result, users);

        CreateChatResponse response = new CreateChatResponse(result);

        return ResponseEntity.ok(response);
    }

    /**
     * Finding users by chat id.
     * @param chatId chat id
     * @return list of users
     */
    public List<UserDto> findUsersFromChatById(final String chatId) {
        return chatRepository.findByChatId(chatId)
                .map(chatRoom -> chatRoom.getUsers()) // Assuming getUsers() already returns List<UserDto>
                .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found with id: " + chatId));
    }

    /**
     * Method returning the chat.
     * @param chatId chat id
     * @return chat
     */
    public ChatRoom getChat(final String chatId) {
        Optional<ChatRoom> chatRoom = chatRepository.findById(chatId);

        return chatRoom.get();
    }

    /**
     * Method that saves chatroom to the database.
     * @param chatRoom chatroom
     */
    public void saveChat(final ChatRoom chatRoom) {
        chatRepository.save(chatRoom);
    }

    /**
     * Get all friended users.
     * @param userId user id
     * @return list of friended users
     */
    public List<UserDto> getUsersInCommonChats(final String userId) {
        // Find all chats that include the user
        List<ChatRoom> chatRooms = chatRepository.findByUserId(userId);

        // Get the users in those chats, excluding the provided user
        return chatRooms.stream()
                .flatMap(chat -> chat.getUsers().stream())
                .filter(user -> !user.getUserId().equals(userId))  // Exclude the current user
                .collect(Collectors.toList());
    }

    /**
     * Getting users ids.
     * @param chatId
     * @return list of user ids
     */
    public List<String> getUserIdsByChatId(final String chatId) {
        Optional<ChatRoom> chatRoom = chatRepository.findById(chatId);
        if (chatRoom.isPresent()) {
            return chatRoom.get().getUsers()
                    .stream()
                    .map(UserDto::getUserId)
                    .collect(Collectors.toList());
        }
        throw new IllegalArgumentException("ChatRoom not found with chatId: " + chatId);
    }

    /**
     * Maps chats to previews.
     * @param chatRooms chat rooms
     * @return list of {@link ChatPreview}
     */
    public List<ChatPreview> toChatPreviewList(final List<ChatRoom> chatRooms) {
        return chatRooms.stream()
                .map(this::toChatPreview)
                .collect(Collectors.toList());
    }

    /**
     * Maps chat to a preview.
     * @param chatRoom chat room
     * @return {@link ChatPreview} object
     */
    private ChatPreview toChatPreview(final ChatRoom chatRoom) {
        var lastMessage = chatRoom.getLastMessage();
        return new ChatPreview(
                chatRoom.getChatId(),
                chatRoom.getUsers(),
                chatRoom.isActive(),
                chatRoom.getPhoto(),
                lastMessage != null ? lastMessage.getContent() : null,
                lastMessage != null ? lastMessage.getSenderId() : null,
                lastMessage != null ? lastMessage.getTimestamp() : null,
                lastMessage != null ? lastMessage.getMessageId() : null
        );
    }

    /**
     * Mapper.
     *
     * @param users - users
     * @return mapped list
     */
    private List<UserDto> mapUsersToDTO(final List<AppUser> users) {
        return users.stream()
                .map(user -> new UserDto(
                        user.getPhoneNumber(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getPhoto()
                ))
                .collect(Collectors.toList());
    }
    /**
     * Retrieves paginated chat rooms for a specific user with optional filtering by chat ID.
     *
     * @param query The user ID to search for and filter chat rooms
     * @param page Zero-based page index
     * @param size The size of the page to be returned
     * @return A page of chat rooms matching the search criteria
     */
    public Page<ChatRoom> getChatRooms(final String query, final int page, final int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ChatRoom> chatRoomsPage = chatRepository.findByUserIdPaged(query, pageable);

        List<ChatRoom> chatRoomResults = new ArrayList<>(chatRoomsPage.getContent());

        return new PageImpl<>(chatRoomResults, pageable, chatRoomsPage.getTotalElements());
    }


}
