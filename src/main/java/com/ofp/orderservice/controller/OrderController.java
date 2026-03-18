package com.ofp.orderservice.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ofp.orderservice.dto.CreateOrderRequest;
import com.ofp.orderservice.entity.Order;
import com.ofp.orderservice.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
		log.info("Received create order request for customer {}", request.getCustomerId());
		Order order = orderService.createOrder(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(order);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Order> getOrder(@PathVariable UUID id) {
		log.info("Received get order request for id {}", id);
		return orderService.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

}
