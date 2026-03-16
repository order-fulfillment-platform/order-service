package com.ofp.orderservice.kafka.outbox;

import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ofp.orderservice.entity.OutboxEvent;
import com.ofp.orderservice.repository.OutboxEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPoller {

	private final OutboxEventRepository outboxEventRepository;
	private final KafkaTemplate<String, String> kafkaTemplate;

	@Scheduled(fixedDelay = 5000)
	@Transactional
	public void pollAndPublish() {

		List<OutboxEvent> events = outboxEventRepository.findByProcessedFalse();

		if(events.isEmpty()) {
			return;
		}

		log.info("Found {} unprocessed outbox events", events.size());

		for (OutboxEvent event : events) {
			try {
				kafkaTemplate.send(resolveTopic(event.getEventType()),
                        event.getAggregateId().toString(),
                        event.getPayload()).get();

				event.setProcessed(true);
				outboxEventRepository.save(event);

				log.info("Published event {} for aggregate {}", event.getEventType(), event.getAggregateId());

			} catch (Exception e) {
				log.error("Failed to publish event {} for aggregate {}", event.getEventType(), event.getAggregateId(), e);
			}
		}
	}

	private String resolveTopic(String eventType) {
		return switch (eventType) {
			case "ORDER_CREATED" -> "order.created";
			default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
		};
	}

}
