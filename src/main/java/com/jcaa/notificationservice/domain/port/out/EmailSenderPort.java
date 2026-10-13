package com.jcaa.notificationservice.domain.port.out;

import com.jcaa.notificationservice.domain.core.model.EmailDestination;

public interface EmailSenderPort {

  void send(EmailDestination destination);
}
