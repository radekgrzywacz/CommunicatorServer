package com.communicator.repositories;

import com.communicator.entity.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * MongoDB repository that manages User entity.
 */
public interface UserRepository extends MongoRepository<AppUser, String> {
    /**
     * Method to get appUser from database by using appUser's phone number.
     * @param phoneNumber - appUser's phone number
     * @return - returns User object
     */
    @Query("{ '_id': ?0 }")
    Optional<AppUser> findByPhoneNumber(String phoneNumber);

    /**
     * Method to get appUser from database by using appUser's email address.
     * @param email - appUser's email adress.
     * @return optional User object
     */
    Optional<AppUser> findByEmail(String email);

    /**
     * Method to get users with dynamic query.
     * @param query - query from the client
     * @param pageable - {@link Pageable} interface to enable pagination
     * @return - all results that match the query
     */
    @Query("{ $and: ["
            + "{ 'validated': true }, "
            + "{ $or: ["
            + "{ '_id': { $regex: '^?0', $options: 'i' } }, "
            + "{ $or: [ "
            + "{ 'first_name': { $regex: '^?0', $options: 'i' } }, "
            + "{ 'last_name': { $regex: '^?0', $options: 'i' } } "
            + "] }"
            + "] }"
            + "] }")
    Page<AppUser> searchUsers(String query, Pageable pageable);


    /**
     * Gets users by their phone numbers.
     * @param ids - users ids
     * @return - found users
     */
    List<AppUser> findByPhoneNumberIn(List<String> ids);

    /**
     * List of users got by set of ids.
     * @param userIdsInChat set of chatids
     * @return list of app users
     */
    List<AppUser> findByPhoneNumberIn(Set<String> userIdsInChat);

}
