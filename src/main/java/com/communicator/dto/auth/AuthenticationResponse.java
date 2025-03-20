package com.communicator.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that holds properties of register method response.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticationResponse {

    private String accessToken;
    private List<String> errors;


    private boolean verified;
    private String firstName;
    private String lastName;

    /**
     * Constructor taking new Access token as parameter.
     * @param token - newly generated access token.
     * @param isVerified - info if user is verified
     * @param usersFirstName - first name
     * @param usersLastName - last name
     */
    public AuthenticationResponse(final String token, final boolean isVerified, final String usersFirstName,
                                  final String usersLastName) {
        this.accessToken = token;
        this.errors = new ArrayList<>(); // Initialize to avoid null reference
        this.verified = isVerified;
        this.firstName = usersFirstName;
        this.lastName = usersLastName;
    }

    /**
     * Constructor taking list of errors as parameter.
     * @param errorsList - list of encountered errors
     */
    public AuthenticationResponse(final List<String> errorsList) {
        this.errors = new ArrayList<>(errorsList);
        this.accessToken = null;
    }

    /**
     * Errors getter.
     * @return list of errors
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors); // Return a copy for immutability
    }

    /**
     * Errors setter.
     * @param errorsList
     */
    public void setErrors(final List<String> errorsList) {
        this.errors = new ArrayList<>(errorsList);
    }

    /**
     * Getter for verified status.
     *
     * @return verification status
     */
    public boolean isVerified() {
        return verified;
    }
}
