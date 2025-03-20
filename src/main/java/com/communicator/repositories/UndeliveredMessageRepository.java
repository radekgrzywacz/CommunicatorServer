package com.communicator.repositories;

import com.communicator.entity.chat.UndeliveredMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

/**
 * Repository with undelivered messages.
 */
public interface UndeliveredMessageRepository extends MongoRepository<UndeliveredMessage, String> {

    /**
     * Finding messages by id.
     * @param userId - user's id
     * @return list of undelivered messages
     */
    List<UndeliveredMessage> findByUserId(String userId);

    /**
     * Deleting old messages.
     * @param userId - user's id
     */
    void deleteByUserId(String userId);
}

