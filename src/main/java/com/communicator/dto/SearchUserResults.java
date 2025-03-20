package com.communicator.dto;

public record SearchUserResults(
        String userId,
        String firstName,
        String lastName,
        String photo
) {
}
