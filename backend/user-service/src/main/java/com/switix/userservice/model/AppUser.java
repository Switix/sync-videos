package com.switix.userservice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDateTime;

@Document(collection = "app_users")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    @Id
    @Field(targetType = FieldType.OBJECT_ID)
    private String id;

    private String username;
    private String password;
    private String userColor;

    @Field(targetType = FieldType.STRING)
    private UserRole role;

    @CreatedDate
    private LocalDateTime createdAt;
}
