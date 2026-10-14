package org.example.Messaging;

import org.example.Entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class OrderPaidPublisher {
    private static final Logger log = LoggerFactory.getLogger(OrderPaidPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public OrderPaidPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${messaging.orders.exchange:orders.exchange}") String exchange,
            @Value("${messaging.orders.paid-routing-key:order.paid}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publish(String eventId, Order order, OffsetDateTime occurredAt) {
        OrderPaidEvent event = OrderPaidEvent.from(
                eventId,
                order.getId(),
                order.getCustomerId(),
                order.getCurrency(),
                order.getTotal(),
                occurredAt);

        rabbitTemplate.convertAndSend(exchange, routingKey, event, message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            message.getMessageProperties().setType("order.paid");
            message.getMessageProperties().setMessageId(eventId);
            return message;
        });

        log.info("Published order.paid eventId={} orderId={} routingKey={}",
                event.eventId(), event.orderId(), routingKey);
    }
}
