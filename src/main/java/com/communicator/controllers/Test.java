package com.communicator.controllers;

import com.communicator.entity.AppUser;
import com.communicator.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Rest controller to check if server is properly initialized.
 */
@RestController
public class Test {

    /**
     * Autowired UserService bean.
     */
    @Autowired
    private UserService userService;

    /**
     * Test method.
     * @return Should return string "Hello World".
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Hello World");
    }

    /**
     * Method handling Get request at '/users/{phoneNumber}.
     * @param phoneNumber - user's phone number
     * @return returns ResponseEntity holding User object that has phoneNumber value as in
     * the request.
     */
    @GetMapping("/users/{phoneNumber}")
    public ResponseEntity<AppUser> getUsers(@PathVariable final String phoneNumber) {
        AppUser appUser = userService.findByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(appUser);
    }

    /**
     * Method handling Get request to get all available users.
     * @return ResponseEntity holding List of User objects.
     */
    @GetMapping("/users")
    public ResponseEntity<List<AppUser>> getAllUsers() {
        List<AppUser> appUsers = userService.findAll();
        return ResponseEntity.ok(appUsers);
    }
}
