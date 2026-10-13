package com.jcaa.notificationservice.adapter.email.request;

import java.util.List;
import java.util.Map;

public record BrevoEmailRequest(
    Contact sender,
    List<Contact> to,
    String subject,
    String htmlContent,
    Map<String, String> headers) {

  public record Contact(String email, String name) {}
}
