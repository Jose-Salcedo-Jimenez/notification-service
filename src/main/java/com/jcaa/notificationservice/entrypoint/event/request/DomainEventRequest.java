package com.jcaa.notificationservice.entrypoint.event.request;

import java.time.LocalDateTime;
import java.util.Map;

public record DomainEventRequest(
    String eventName, LocalDateTime occurredOn, Map<String, String> payload) {}
