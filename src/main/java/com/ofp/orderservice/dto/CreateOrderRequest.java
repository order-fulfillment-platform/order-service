package com.ofp.orderservice.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {

	@NotNull(message = "Customer ID is required" )
	private UUID customerId;

	@NotNull(message="Items are required")
	@NotEmpty(message = "Order must contain at least one item")
	private List<OrderItemRequest> items;
}
