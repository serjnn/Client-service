package com.serjnn.ClientService.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String mail;
    private String password;
}
