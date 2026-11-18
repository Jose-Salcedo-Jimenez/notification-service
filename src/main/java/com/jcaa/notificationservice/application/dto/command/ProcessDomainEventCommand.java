package com.jcaa.notificationservice.application.dto.command;

import java.util.Map;

public record ProcessDomainEventCommand(
    String eventName, String occurredOn, Map<String, String> payload) {}
