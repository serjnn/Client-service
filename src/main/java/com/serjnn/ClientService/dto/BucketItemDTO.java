package com.serjnn.ClientService.dto;

import java.math.BigDecimal;

public record BucketItemDTO(Long id, String name, Integer quantity, BigDecimal price) {}
