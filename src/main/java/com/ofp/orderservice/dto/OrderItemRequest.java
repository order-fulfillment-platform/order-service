package com.ofp.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequest {

	@NotNull(message = "Product ID is required")
	private UUID productId;

	@NotNull(message = "Quantitiy is required")
	@Min(value = 1, message = "Quantity must be at least 1")
	private Integer quantity;

	@NotNull(message = "Unit price is required")
	@DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
	private BigDecimal unitPrice;
}
