package com.communicator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserDto {
    private String userId;
    private String firstName;
    private String lastName;
    private String photo = "https://img.freepik.com/premium-vector/illustration-persons-face-outline-icon-symbolizing"
            + "-anonymity_1171540-20820.jpg";
}
