package com.communicator.services;

import com.communicator.dto.auth.AuthenticationResponse;
import com.communicator.dto.auth.CodeValidationRequest;
import com.communicator.dto.auth.EmailServiceRequest;
import com.communicator.dto.auth.PasswordResetRequest;
import com.communicator.dto.auth.PasswordResetResponse;
import com.communicator.dto.auth.UserCreationResponse;
import com.communicator.dto.auth.UserLoginDto;
import com.communicator.dto.auth.RequestResponse;
import com.communicator.entity.AppUser;
import com.communicator.entity.Token;
import com.communicator.repositories.TokenRepository;
import com.communicator.repositories.UserRepository;
import com.communicator.services.utils.EmailService;
import com.communicator.services.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Service class for handling user authentication and authorization methods like
 * register, login, saving new user
 * to database etc.
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private EmailService emailService;


    /**
     * Method for registering and saving new user to the database. This method
     * handles field verification.
     *
     * @param request       - User class object passed from the client. The name
     *                      'request' is used for user before
     *                      confirming if new User can be created.
     * @param bindingResult - Binding Result object to check for field errors
     * @return new Authentication Response object
     */
    public ResponseEntity<UserCreationResponse> register(final AppUser request, final BindingResult bindingResult) {
        List<String> errors = new ArrayList<>();
        if (bindingResult.hasErrors()) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                errors.add(fieldError.getDefaultMessage());
            }
        }

        AppUser appUserExistanceCheck = userRepository.findByPhoneNumber(request.getPhoneNumber()).orElse(null);
        if (appUserExistanceCheck != null) {
            if (!appUserExistanceCheck.isValidated()) {
                return ResponseEntity.status(206).body(null);
            }
            errors.add("Phone number already exists!\n");
        }

        appUserExistanceCheck = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (appUserExistanceCheck != null) {
            if (!appUserExistanceCheck.isValidated()) {
                return ResponseEntity.status(206).body(null);
            }
            errors.add("Email already exists!\n");
        }

        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(new UserCreationResponse(errors));
        }

        AppUser appUser = new AppUser();
        appUser.setPhoneNumber(request.getPhoneNumber());
        appUser.setEmail(request.getEmail());
        appUser.setFirstName(request.getFirstName());
        appUser.setLastName(request.getLastName());
        appUser.setPassword(passwordEncoder.encode(request.getPassword()));
        appUser.setValidated(false);

        appUser = userRepository.save(appUser);

        if (appUser.getPhoneNumber() != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(null);
        } else {
            errors.add("Couldn't save user!\n");
            return ResponseEntity.badRequest().body(new UserCreationResponse(errors));
        }

    }

    /**
     * Method for logging user in and generating new access token.
     *
     * @param request Dto with credentials required to log in.
     * @return Response entity with logging in status.
     */
    public ResponseEntity<AuthenticationResponse> login(final UserLoginDto request) {
        try {
            if (request.getEmail() == null) {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getPhoneNumber(),
                        request.getPassword()));
            } else {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),
                        request.getPassword()));
            }
        } catch (Exception e) {
            List<String> errors = new ArrayList<>();
            errors.add("Bad credentials!\n");
            return ResponseEntity.badRequest().body(new AuthenticationResponse(errors));
        }

        AppUser appUser = request.getEmail() == null
                ? userRepository.findByPhoneNumber(request.getPhoneNumber()).orElseThrow()
                : userRepository.findByEmail(request.getEmail()).orElseThrow();
        String accessToken = jwtUtils.generateAccessToken(appUser);
        saveUserToken(accessToken, appUser);

        return ResponseEntity.ok().body(new AuthenticationResponse(accessToken, appUser.isValidated(),
                appUser.getFirstName(), appUser.getLastName()));
    }

    private void saveUserToken(final String jwt, final AppUser appUser) {
        Token token = new Token();
        token.setToken(jwt);
        token.setPhoneNumber(appUser.getPhoneNumber());
        tokenRepository.save(token);
    }

    /**
     * Method for sending a verification email.
     *
     * @param request request with user's data
     * @return returns ResponseEntity with status OK or BAD REQUEST
     */
    public ResponseEntity<RequestResponse> sendVerificationEmail(final EmailServiceRequest request) {
        final int codeLength = 5;
        Optional<String> emailOptional = resolveEmail(request.getEmail(), request.getPhoneNumber());
        if (emailOptional.isEmpty()) {
            RequestResponse validationResponse = new RequestResponse();
            validationResponse.setMessage("No user's information found.");
            return ResponseEntity.badRequest().body(validationResponse);
        }
        String email = emailOptional.get();

        Optional<AppUser> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            RequestResponse validationResponse = new RequestResponse();
            validationResponse.setMessage("No user found.");
            return ResponseEntity.badRequest().body(validationResponse);
        }

        AppUser appUser = optionalUser.get();
        String code = generateRandomString(codeLength);
        String subject = "Communicator App Verification Code";
        String title = "Account verification";
        String message = "You want to create an account on Communicator App. Here is your verification code:";
        boolean emailSent = emailService.sendEmail(appUser.getEmail(), title, subject, message, code);
        if (!emailSent) {
            RequestResponse validationResponse = new RequestResponse();
            validationResponse.setMessage("Could not send an email");
            return ResponseEntity.badRequest().body(validationResponse);
        }
        appUser.setVerificationCode(code);
        userRepository.save(appUser);

        return ResponseEntity.ok().build();
    }


    /**
     * Resolves the user's email based on the provided email or phone number.
     * If the email is null and a phone number is provided, the method attempts to retrieve
     * the email associated with that phone number from the database.
     *
     * @param email       the email address provided in the request, may be null
     * @param phoneNumber the phone number provided in the request, may be null
     * @return an Optional containing the email address if found, or an empty Optional if
     * the email could not be resolved (e.g., if no user is associated with the provided phone number)
     */
    private Optional<String> resolveEmail(final String email, final String phoneNumber) {
        if (email == null && phoneNumber != null) {
            Optional<AppUser> optionalUserByPhone = userRepository.findByPhoneNumber(phoneNumber);
            if (optionalUserByPhone.isPresent()) {
                return Optional.of(optionalUserByPhone.get().getEmail());
            } else {
                return Optional.empty(); // No user found with that phone number
            }
        }
        return Optional.ofNullable(email);
    }

    /**
     * Method verifying code given by the user. Supports user's verification using either email or phone number.
     *
     * @param request object holding user's email and verification code
     * @return VerificationResponse with message and HttpStatus indicating if
     * verification succeeded
     */
    public ResponseEntity<RequestResponse> verifyAccount(final CodeValidationRequest request) {
        Optional<String> emailOptional = resolveEmail(request.getEmail(), request.getPhoneNumber());
        if (emailOptional.isEmpty()) {
            RequestResponse validationResponse = new RequestResponse();
            validationResponse.setMessage("No user's data found.");
            return ResponseEntity.badRequest().body(validationResponse);
        }
        String email = emailOptional.get();

        Optional<AppUser> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            RequestResponse validationResponse = new RequestResponse();
            validationResponse.setMessage("No user found.");
            return ResponseEntity.badRequest().body(validationResponse);
        }

        AppUser appUser = optionalUser.get();
        if (!appUser.getVerificationCode().equals(request.getCode())) {
            RequestResponse validationResponse = new RequestResponse();
            validationResponse.setMessage("Incorrect verification code.");
            return ResponseEntity.badRequest().body(validationResponse);
        }
        RequestResponse validationResponse = new RequestResponse();
        validationResponse.setMessage("Verification was successful!");
        appUser.setValidated(true);
        appUser.setVerificationCode(null);
        userRepository.save(appUser);
        return ResponseEntity.ok().body(validationResponse);
    }

    /**
     * Method sending a password reset email. Supports password reset using either email or phone number.
     *
     * @param request user's email or phoneNumber
     * @return returns ResponseEntity with status OK or BAD REQUEST
     */
    public ResponseEntity<RequestResponse> sendPasswordResetEmail(final EmailServiceRequest request) {
        final int codeLength = 5;

        Optional<String> emailOptional = resolveEmail(request.getEmail(), request.getPhoneNumber());
        if (emailOptional.isEmpty()) {
            RequestResponse validationResponse = new RequestResponse();
            validationResponse.setMessage("No user's data found.");
            return ResponseEntity.badRequest().body(validationResponse);
        }
        String email = emailOptional.get();

        Optional<AppUser> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            RequestResponse validationResponse = new RequestResponse();
            validationResponse.setMessage("No user found.");
            return ResponseEntity.badRequest().body(validationResponse);
        }

        AppUser appUser = optionalUser.get();
        String code = generateRandomString(codeLength);
        String subject = "Communicator App Password Reset";
        String message = "You requested resetting your password. Here is your code: ";
        String title = "Password reset";
        boolean emailSent = emailService.sendEmail(email, title, subject, message, code);
        if (!emailSent) {
            RequestResponse validationResponse = new RequestResponse();
            validationResponse.setMessage("Could not send an email");
            return ResponseEntity.badRequest().body(validationResponse);
        }
        appUser.setPasswordResetCode(code);
        userRepository.save(appUser);

        RequestResponse response = new RequestResponse();
        response.setMessage(appUser.getEmail());

        return ResponseEntity.ok().body(response);
    }


    /**
     * Method verify reset code send by server.
     *
     * @param request users's email and code
     * @return returns ResponseEntity with status OK or BAD REQUEST
     */
    public ResponseEntity<RequestResponse> verifyResetCode(final CodeValidationRequest request) {
        Optional<String> emailOptional = resolveEmail(request.getEmail(), request.getPhoneNumber());
        if (emailOptional.isEmpty()) {
            RequestResponse validationResponse = new RequestResponse();
            validationResponse.setMessage("No user's data found.");
            return ResponseEntity.badRequest().body(validationResponse);
        }
        String email = emailOptional.get();

        Optional<AppUser> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            RequestResponse validationResponse = new RequestResponse();
            validationResponse.setMessage("No user found.");
            return ResponseEntity.badRequest().body(validationResponse);
        }
        AppUser appUser = optionalUser.get();
        if (!appUser.getPasswordResetCode().equals(request.getCode())) {
            RequestResponse validationResponse = new RequestResponse();
            validationResponse.setMessage("Code not matched.");
            return ResponseEntity.badRequest().body(validationResponse);
        }

        return ResponseEntity.ok().build();


    }

    /**
     * A method that allows changing the password and checks its validity and uniqueness.
     *
     * @param request       user's email, code and new password
     * @param bindingResult password validation
     * @return ResponseEntity with status message
     */
    public ResponseEntity<PasswordResetResponse> resetPassword(final PasswordResetRequest request,
                                                               final BindingResult bindingResult) {

        List<String> errors = new ArrayList<>();

        if (bindingResult.hasErrors()) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                errors.add(fieldError.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(new PasswordResetResponse(errors));
        }

        Optional<AppUser> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body(new PasswordResetResponse("User not found."));
        }

        AppUser appUser = optionalUser.get();

        if (!appUser.getPasswordResetCode().equals(request.getCode())) {
            errors.add("Incorrect verification code.");
        }

        if (passwordEncoder.matches(request.getPassword(), appUser.getPassword())) {
            errors.add("New password cannot be the same as the old password.");
        }

        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(new PasswordResetResponse(errors));
        }

        appUser.setPassword(passwordEncoder.encode(request.getPassword()));
        appUser.setPasswordResetCode(null);
        userRepository.save(appUser);

        String message = "Password reset was successful!";
        PasswordResetResponse response = new PasswordResetResponse(message);
        return ResponseEntity.ok().body(response);


    }

    /**
     * Private method to generate random alphanumeric string. Filtered values
     * exclude punctuation numerals.
     *
     * @param targetStringLength length of the returned string
     * @return new random String
     */
    private String generateRandomString(final int targetStringLength) {
        final int leftLimit = 48; // numeral '0'
        final int rightLimit = 122; // letter 'z'
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

    }
}
