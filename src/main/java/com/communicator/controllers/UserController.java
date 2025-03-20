package com.communicator.controllers;

import com.communicator.dto.SearchResultDto;
import com.communicator.dto.SearchUserResults;
import com.communicator.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /**
     * Method for dynamic serching in user database with query given from the client.
     *
     * @param query - parameter from client
     * @param page - page number
     * @param size - number of objects per page
     * @return - page with given number of {@link SearchUserResults} objects
     */
    @GetMapping("/search")
    public ResponseEntity<SearchResultDto> getUsers(final @RequestParam String query,
                                                    final @RequestParam(defaultValue = "0") int page,
                                                    final @RequestParam(defaultValue = "10") int size) {
//        String searchQuery;
//        if (query.matches("\\d+")) {
//            searchQuery = "\\+" + query;
//        } else {
//            searchQuery = query;
//        }
        Page<SearchUserResults> foundUsers = userService.searchUsers(query, page, size);
        if (foundUsers.getTotalElements() > 0) {
            SearchResultDto searchResultDto = new SearchResultDto();
            searchResultDto.setResults(foundUsers);
            return ResponseEntity.ok(searchResultDto);
        }

        SearchResultDto searchResultDto = new SearchResultDto();
        searchResultDto.setMessage("No users found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(searchResultDto);

    }

}
