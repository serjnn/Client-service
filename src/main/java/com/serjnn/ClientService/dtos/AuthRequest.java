package com.serjnn.ClientService.dtos;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}
