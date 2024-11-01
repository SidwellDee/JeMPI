package org.jembi.jempi.shared.models;

import java.time.LocalDateTime;

public record MpiMediator(
      LocalDateTime createdAt,
      String interactionID,
      String goldenID,
      String event) {
}
