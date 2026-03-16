package com.ofp.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Data;

@Data
public class OrderItemRequest {
	private UUID productId;
	private Integer quantity;
	private BigDecimal unitPrice;
}
