package com.communicator.services.chat;

import com.communicator.entity.chat.ChatMessage;
import com.communicator.repositories.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    /**
     * Method that saves chatroom to the database.
     * @param chatMessage chat message
     */
    public void saveChatMessage(final ChatMessage chatMessage) {
        chatMessageRepository.save(chatMessage);
    }

    /**
     * Retrieves a paginated list of chat messages that match the given query.
     * The results are returned as a {@link Page} object, maintaining the order
     * provided by the repository.
     *
     * @param query the query string used to filter chat messages by their message ID
     * @param page the zero-based page index to retrieve
     * @param size the number of items to include in a single page
     * @return a {@link Page} containing the filtered chat messages
     */
    public Page<ChatMessage> getChatMessages(final String query, final int page, final int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessage> chatMessagesPage = chatMessageRepository.findByMessageId(query, pageable);

        return new PageImpl<>(chatMessagesPage.getContent(), pageable, chatMessagesPage.getTotalElements());
    }

    /**
     * Message that retrieves all messages before the last one loaded on the client side.
     * @param chatId chat id
     * @param messageId last message id
     * @param page number of page
     * @param size page size
     * @return page of chat messages
     */
    public Page<ChatMessage> getChatMessagesBefore(final String chatId, final String messageId, final int page,
                                                   final int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<ChatMessage> chatMessagesPage = chatMessageRepository.findMessagesBefore(chatId, messageId, pageable);
        return new PageImpl<>(chatMessagesPage.getContent(), pageable, chatMessagesPage.getTotalElements());
    }
}
