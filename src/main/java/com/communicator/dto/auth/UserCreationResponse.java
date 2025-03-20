package com.communicator.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Dto class that is being returned from {@link com.communicator.controllers.AuthController register} method.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserCreationResponse {
    private List<String> errors = new ArrayList<>();

    /**
     * Constructor with errors list.
     * @param errorsList
     */
    public UserCreationResponse(final List<String> errorsList) {
        this.errors = new ArrayList<>(errorsList);
    }

    /**
     * Errors setter.
     * @param errorsList
     */
    public void setErrors(final List<String> errorsList) {
        this.errors = new ArrayList<>(errorsList);
    }

    /**
     * Errors getter.
     * @return errors
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
}
