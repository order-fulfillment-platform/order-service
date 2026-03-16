package com.ofp.orderservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ofp.orderservice.dto.CreateOrderRequest;
import com.ofp.orderservice.dto.OrderItemRequest;
import com.ofp.orderservice.entity.Order;
import com.ofp.orderservice.entity.OrderStatus;
import com.ofp.orderservice.repository.OrderRepository;
import com.ofp.orderservice.repository.OutboxEventRepository;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OutboxEventRepository outboxEventRepository;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private OrderService orderService;

	@Test
	void createOrder_shouldReturnSavedOrder() throws Exception {
		// Arrange
		UUID customerId = UUID.randomUUID();

		OrderItemRequest itemRequest = new OrderItemRequest();
		itemRequest.setProductId(UUID.randomUUID());
		itemRequest.setQuantity(2);
		itemRequest.setUnitPrice(new BigDecimal("29.99"));

		CreateOrderRequest request = new CreateOrderRequest();
		request.setCustomerId(customerId);
		request.setItems(List.of(itemRequest));

		Order savedOrder = Order.builder()
				.id(UUID.randomUUID())
				.customerId(customerId)
				.status(OrderStatus.PENDING)
				.totalAmount(new BigDecimal("59.98"))
				.build();

		when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
		when(objectMapper.writeValueAsString(any())).thenReturn("{}");
		when(outboxEventRepository.save(any())).thenReturn(null);

		// Act
		Order result = orderService.createOrder(request);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getCustomerId()).isEqualTo(customerId);
		assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
		verify(orderRepository, times(1)).save(any(Order.class));
		verify(outboxEventRepository, times(1)).save(any());
	}


	@Test
	void createOrder_shouldCalculateTotalAmountCorrectly() throws Exception {
		// Arrange
		OrderItemRequest item1 = new OrderItemRequest();
		item1.setProductId(UUID.randomUUID());
		item1.setUnitPrice(new BigDecimal("29.99"));
		item1.setQuantity(2);


		OrderItemRequest item2 = new OrderItemRequest();
		item2.setProductId(UUID.randomUUID());
		item2.setUnitPrice(new BigDecimal("49.99"));
		item2.setQuantity(1);

		CreateOrderRequest request = new CreateOrderRequest();
		request.setCustomerId(UUID.randomUUID());
		request.setItems(List.of(item1, item2));

		when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
		when(objectMapper.writeValueAsString(any())).thenReturn("{}");
		when(outboxEventRepository.save(any())).thenReturn(null);

		// Act
		Order result = orderService.createOrder(request);

		// Assert
		assertThat(result.getTotalAmount())
			.isEqualByComparingTo(new BigDecimal("109.97"));


	}

}
