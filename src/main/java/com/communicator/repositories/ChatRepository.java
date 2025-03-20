package com.communicator.repositories;

import com.communicator.entity.chat.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends MongoRepository<ChatRoom, String> {

    /**
     * Method that retrieves all chats with this exact phone numbers.
     *
     * @param phoneNumbers
     * @return duplicated chats
     */
    @Query("{ 'users.userId': { $all: ?0 }, $expr: { $eq: [ { $size: '$users' }, 2 ] } }")
    List<ChatRoom> findByExactUsersPhoneNumber(List<String> phoneNumbers);

    /**
     * Finding chat by id.
     * @param chatId chat's id
     * @return new chat
     */
    @Query("{ '_id': ?0 }")
    Optional<ChatRoom> findByChatId(String chatId);

    /**
     * Method to get chats with dynamic query.
     * @param userId user id
     * @return - all results that match the query
     */
    @Query("{ 'users.userId': ?0 }")
    List<ChatRoom> findByUserId(String userId);

    /**
     * Method to get paged chats.
     * @param userId user id
     * @param pageable the pagination information to retrieve a subset of the results.
     * @return - all results that match the query
     */
    @Query("{ 'users.userId': ?0 }")
    Page<ChatRoom> findByUserIdPaged(String userId, Pageable pageable);


    /**
     * Method to get chats with dynamic query limited to ten.
     * @param userId user id
     * @return - all results that match the query
     */
    @Query("{ 'users.userId': ?0 }")
    List<ChatRoom> findTop10ByUserId(String userId);
}
