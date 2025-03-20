package com.communicator.repositories;

import com.communicator.entity.chat.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    /**
     * Method to get 20 last messages.
     * @param chatId id of chat
     * @return all results that match the query
     */
    List<ChatMessage> findTop20ByChatIdOrderByTimestampDesc(String chatId);

    /**
     * Method to get chat messages send before.
     * @param chatId id of chat
     * @param messageId id of last message
     * @param pageable the pagination information
     * @return all results that match the query
     */
    @Query(value = "{ 'chatId': ?0, 'messageId': { $lt: ?1 } }", sort = "{ 'timestamp': -1 }")
    Page<ChatMessage> findMessagesBefore(String chatId, String messageId, Pageable pageable);

    /**
     * Method to found by messageId.
     * @param query query
     * @param pageable the pagination information
     * @return null
     */
    Page<ChatMessage> findByMessageId(String query, Pageable pageable);
}


