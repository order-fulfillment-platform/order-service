package com.ofp.orderservice.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ofp.orderservice.dto.CreateOrderRequest;
import com.ofp.orderservice.entity.Order;
import com.ofp.orderservice.entity.OrderItem;
import com.ofp.orderservice.entity.OutboxEvent;
import com.ofp.orderservice.repository.OrderRepository;
import com.ofp.orderservice.repository.OutboxEventRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

	private final OrderRepository orderRepository;
	private final OutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper;

	@Transactional
	public Order createOrder(CreateOrderRequest request) {
		// Build OrderItems
		List<OrderItem> items = request.getItems().stream()
				.map(itemRequest -> OrderItem.builder()
						.productId(itemRequest.getProductId())
						.quantity(itemRequest.getQuantity())
						.unitPrice(itemRequest.getUnitPrice())
						.build())
				.toList();

		// Calculate total amount
		BigDecimal totalAmount = items.stream()
				.map(item -> item.getUnitPrice()
						.multiply(BigDecimal.valueOf(item.getQuantity())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		// Build the Order
		Order order = Order.builder()
				.customerId(request.getCustomerId())
				.totalAmount(totalAmount)
				.build();


		// Link items to the order
		items.forEach(item -> item.setOrder(order));
		order.getItems().addAll(items);

		// Save the order
		Order saveOrder = orderRepository.save(order);

		// Create outbox event within the same transaction
		try {
			String payload = objectMapper.writeValueAsString(saveOrder);
			OutboxEvent event = OutboxEvent.builder()
					.aggregateId(saveOrder.getId())
					.eventType("ORDER_CREATED")
					.payload(payload)
					.build();

			outboxEventRepository.save(event);
		} catch (JsonProcessingException e) {
			log.error("Failed to create outbox event for order {}", saveOrder.getId());
			throw new RuntimeException("Failed to ccreate outbox event", e);
		}

		log.info("Order created with id {}", saveOrder.getId());
		return saveOrder;
	}

	public Optional<Order> findById(UUID id) {
		return orderRepository.findById(id);
	}
}
