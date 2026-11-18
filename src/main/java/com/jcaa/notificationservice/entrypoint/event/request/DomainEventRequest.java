package com.jcaa.notificationservice.entrypoint.event.request;

import java.util.Map;

public record DomainEventRequest(String eventName, String occurredOn, Map<String, String> payload) {}
