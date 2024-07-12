package com.switix.userservice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserVO {
    private String id;
    private String username;
    private String password;
    private String userColor;
    private String role;
}
