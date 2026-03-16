package com.ofp.orderservice.dto;

import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class CreateOrderRequest {
	private UUID customerId;
	private List<OrderItemRequest> items;
}
