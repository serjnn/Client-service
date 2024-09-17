package com.serjnn.ClientService.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;


@AllArgsConstructor
@Getter
public class ClientInfoDto {
    private final Long id;
    private final String mail;
    private final BigDecimal balance;
    private final String address;
}