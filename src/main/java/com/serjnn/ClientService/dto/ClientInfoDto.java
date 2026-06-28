package com.serjnn.ClientService.dto;

import java.math.BigDecimal;

public record ClientInfoDto(Long id, String mail, BigDecimal balance, String address) {}
