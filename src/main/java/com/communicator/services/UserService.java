package com.communicator.services;

import com.communicator.dto.SearchUserResults;
import com.communicator.entity.AppUser;
import com.communicator.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for handling User data.
 */
@Service
@Slf4j
public class UserService implements UserDetailsService {

    /**
     * Autowired UserRepository bean.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Service method to call repository method that gets user by their's phone number.
     * @param phoneNumber - User's phone number passed from request url variable
     * @return - return User class object
     */
    public AppUser findByPhoneNumber(final String phoneNumber) {
        log.warn(phoneNumber);
        return userRepository.findByPhoneNumber(phoneNumber).orElse(null);
    }

    /**
     * Service method to call repository method that gets all users from database.
     * @return - List of User objects.
     */
    public List<AppUser> findAll() {
        return userRepository.findAll();
    }

    /**
     * Method from implementing UserDetailsService interface. NOT IN USE.
     * @param input - phone number or password
     * @return - null because it's not used.
     * @throws UsernameNotFoundException - in case of searching for non existing user.
     */
    @Override
    public UserDetails loadUserByUsername(final String input) throws UsernameNotFoundException {
        AppUser appUser;

        if (input.contains("@")) {
            appUser = userRepository.findByEmail(input)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + input));
        } else {
            appUser = userRepository.findByPhoneNumber(input)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with phone number: " + input));
        }

        // Return a valid UserDetails object, such as the Spring Security User
        return new org.springframework.security.core.userdetails.User(
                appUser.getPhoneNumber(),
                appUser.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    /**
     * Class that gets all matching users from the database.
     * @param query - query from client
     * @param page - results page number
     * @param size - page size
     * @return - {@link Page} with {@link SearchUserResults} objects
     */
    public Page<SearchUserResults> searchUsers(final String query, final int page, final int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AppUser> appUsersPage = userRepository.searchUsers(query, pageable);

        List<SearchUserResults> searchUserResults = appUsersPage.getContent().stream()
                .map(this::mapToSearchUserResults)
                .collect(Collectors.toList());


        return new PageImpl<>(searchUserResults, pageable, appUsersPage.getTotalElements());
    }

    private SearchUserResults mapToSearchUserResults(final AppUser appUser) {
        return new SearchUserResults(
                appUser.getPhoneNumber(),
                appUser.getFirstName(),
                appUser.getLastName(),
                appUser.getPhoto()
        );
    }

}
