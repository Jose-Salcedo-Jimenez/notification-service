package com.jcaa.notificationservice.application.dto.command;

import java.time.LocalDateTime;
import java.util.Map;

public record ProcessDomainEventCommand(
    String eventName, LocalDateTime occurredOn, Map<String, String> payload) {}
