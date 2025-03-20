package com.communicator.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Entity class to map database Tokens collection.
 */
@Data
@Document("tokens")
public class Token {

    @Field("phone_number")
    private String phoneNumber;
    private String token;
}
