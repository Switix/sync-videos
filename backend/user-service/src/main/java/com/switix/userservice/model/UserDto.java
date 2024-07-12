package com.switix.userservice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {
    private String id;
    private String username;
    private String userColor;
    private String role;
}
