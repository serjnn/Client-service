package com.serjnn.ClientService.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
public class OrderDTO {
    private UUID orderId;

    private Long clientID;


    private List<BucketItemDTO> items;


    private BigDecimal totalSum;

}