package com.ofp.orderservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ofp.orderservice.dto.CreateOrderRequest;
import com.ofp.orderservice.dto.OrderItemRequest;
import com.ofp.orderservice.repository.OrderRepository;
import com.ofp.orderservice.repository.OutboxEventRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class OrderControllerIntegrationTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

	@Container
	static ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

	@DynamicPropertySource
	static void overrideProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private OutboxEventRepository outboxEventRepository;

	@BeforeEach
	void setUp() {
		outboxEventRepository.deleteAll();
		orderRepository.deleteAll();
	}

	@Test
	void createOrder_shouldReturn201AndPersistOrder() throws Exception {
		// Arrange
		OrderItemRequest item = new OrderItemRequest();
		item.setProductId(UUID.randomUUID());
		item.setUnitPrice(new BigDecimal("29.99"));
		item.setQuantity(2);

		CreateOrderRequest request = new CreateOrderRequest();
		request.setCustomerId(UUID.randomUUID());
		request.setItems(List.of(item));

		// Act & Assert
		mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(59.98));

		// Verify that the order has been saved in DB
		assertThat(orderRepository.findAll()).hasSize(1);

		// Verify that the outbox event has been created
		assertThat(outboxEventRepository.findByProcessedFalse()).hasSize(1);
		assertThat(outboxEventRepository.findByProcessedFalse().get(0).getEventType())
			.isEqualTo("ORDER_CREATED");
	}

	@Test
	void getOrder_shouldReturn404WhenNotFound() throws Exception {
		mockMvc.perform(get("/api/v1/orders/" + UUID.randomUUID()))
			.andExpect(status().isNotFound());
	}

	@Test
	void createOrder_shouldCreateOutoboxEventWithCorrectPayload() throws Exception {
		// Arrange
		UUID customerId = UUID.randomUUID();

		OrderItemRequest item = new OrderItemRequest();
		item.setProductId(UUID.randomUUID());
		item.setUnitPrice(new BigDecimal("99.99"));
		item.setQuantity(1);

		CreateOrderRequest request = new CreateOrderRequest();
		request.setCustomerId(customerId);
		request.setItems(List.of(item));

		// Act
		mockMvc.perform(post("/api/v1/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated());

		// Assert outbox
		var events = outboxEventRepository.findByProcessedFalse();
		assertThat(events).hasSize(1);
		assertThat(events.get(0).getEventType()).isEqualTo("ORDER_CREATED");
		assertThat(events.get(0).getPayload()).contains(customerId.toString());
	}



}
