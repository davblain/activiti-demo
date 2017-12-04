package org.korbit.test.activiti.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class LoginResponse {
    String username;
    String token;
    Date expirationTime;
}
