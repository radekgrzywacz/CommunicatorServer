package com.communicator.repositories;

import com.communicator.entity.Token;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;


/**
 * TokenRepository is an interface for managing {@link com.communicator.entity.Token} entities in a
 * MongoDB database.
 * It extends {@link org.springframework.data.mongodb.repository.MongoRepository} to provide CRUD
 * operations.
 */
public interface TokenRepository extends MongoRepository<Token, String> {

    /**
     * Retrieves all tokens associated with the specified phone number.
     *
     * @param phoneNumber the phone number of the user
     * @return an Optional containing a list of tokens if found, or empty if none exist
     */
    Optional<List<Token>> findAllTokensByPhoneNumber(String phoneNumber);
}
