package com.communicator.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeValidationRequest {
    private String email;
    private String phoneNumber;
    private String code;
}
