package com.communicator.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PasswordResetResponse {
    private String message;
    private List<String> errors;

    /**
     * Constructor to create object if password is valid.
     * @param responseMessage response message
     */
    public PasswordResetResponse(final String responseMessage) {
        this.message = responseMessage;
    }


    /**
     * Constructor to create object if password is not valid.
     * @param passwordErrors - password errors
     */
    public PasswordResetResponse(final List<String> passwordErrors) {
        this.errors = passwordErrors;
    }
}
