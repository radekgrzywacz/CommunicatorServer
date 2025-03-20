package com.communicator.controllers;

import com.communicator.dto.auth.AuthenticationResponse;
import com.communicator.dto.auth.CodeValidationRequest;
import com.communicator.dto.auth.EmailServiceRequest;
import com.communicator.dto.auth.PasswordResetRequest;
import com.communicator.dto.auth.PasswordResetResponse;
import com.communicator.dto.auth.UserCreationResponse;
import com.communicator.dto.auth.UserLoginDto;
import com.communicator.dto.auth.RequestResponse;
import com.communicator.entity.AppUser;
//import com.communicator.dto.*;
import com.communicator.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;


/**
 * Rest controller to handle authentication and authorization requests with field validation.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Method handling Post requests to validate and register new User.
     *
     * @param appUser          User object mapped from request body
     * @param bindingResult BindingResult object to collect property validation errors.
     * @return ResponseEntity with Authentication response object.
     */
    @PostMapping("/register")
    public ResponseEntity<UserCreationResponse> register(final @Valid @RequestBody AppUser appUser,
                                                         final BindingResult bindingResult) {
        return authService.register(appUser, bindingResult);
    }

    /**
     * Method handling Post requests to login existing user.
     *
     * @param loginDto UserLoginDto with minimal user credentials needed to log in.
     * @return Response entity with Authentication Response object
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(final @RequestBody UserLoginDto loginDto) {
        return authService.login(loginDto);
    }

    /**
     * Controller sending a verification email.
     *
     * @param email user email from request body
     * @return returns ResponseEntity with status OK or BAD REQUEST
     */
    @PostMapping("/verification/email")
    public ResponseEntity<RequestResponse> verificationEmail(final @RequestBody EmailServiceRequest email) {
        return authService.sendVerificationEmail(email);
    }

    /**
     * Controller that calls code verification method.
     *
     * @param request CodeValidationRequest object holding user's email and verification code
     * @return VerificationResponse with message and HttpStatus indicating if verification succeeded
     */
    @PostMapping("/verification")
    public ResponseEntity<RequestResponse> verify(final @RequestBody CodeValidationRequest request) {
        return authService.verifyAccount(request);
    }

    /**
     * Controller sending a password reset email
     *
     * @param email user's email from request body
     * @return ResponseEntity with correct status
     */
    @PostMapping("/password-reset/email")
    public ResponseEntity<RequestResponse> passwordResetEmail(final @RequestBody EmailServiceRequest email) {
        return authService.sendPasswordResetEmail(email);
    }

    /**
     * Controller for checking reset password's code.
     *
     * @param request CodeValidationRequest object holding user's email and verification code
     * @return Void with code indicating if verification succeeded
     */
    @PostMapping("/password-reset/code")
    public ResponseEntity<RequestResponse> passwordResetCode(final @RequestBody CodeValidationRequest request) {
        return authService.verifyResetCode(request);
    }

    /**
     * Endpoint calling a method that verifies given code, password and if correct changes the password in database.
     *
     * @param email         Dto having user's email, code and new password
     * @param bindingResult Entity properties validation
     * @return Dto indicating password reset success
     */
    @PostMapping("/password-reset")
    public ResponseEntity<PasswordResetResponse> passwordResetEmail(
            final @Valid @RequestBody PasswordResetRequest email,
            final BindingResult bindingResult) {
        return authService.resetPassword(email, bindingResult);
    }


}
