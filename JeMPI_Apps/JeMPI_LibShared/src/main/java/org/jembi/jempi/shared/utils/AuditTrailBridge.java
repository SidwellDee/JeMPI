package org.jembi.jempi.shared.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;
import org.jembi.jempi.shared.models.AuditEvent;
import org.jembi.jempi.shared.models.GlobalConstants;

import java.sql.Timestamp;
import java.util.UUID;

import static org.jembi.jempi.shared.utils.AppUtils.OBJECT_MAPPER;


public final class AuditTrailBridge {
   private static final Logger LOGGER = LogManager.getLogger(AuditTrailBridge.class);
   private final MyKafkaProducer<String, AuditEvent> topicAuditEvents;

   public AuditTrailBridge(final MyKafkaProducer<String, AuditEvent> topicAuditEvents) {
      this.topicAuditEvents = topicAuditEvents;
   }

   public static <T> String getSerializedEventData(final T eventData) {
      try {
         return eventData != null
               ? OBJECT_MAPPER.writeValueAsString(eventData)
               : null;
      } catch (JsonProcessingException e) {
         LOGGER.error("Failed to serialize event data", e);
         return null;
      }
   }

   public static <T> T getDeserializeEventData(
         final String eventData,
         final Class<T> valueType) throws JsonProcessingException {
      return OBJECT_MAPPER.readValue(eventData, valueType);
   }

   public <T> void sendAuditEvent(
         final GlobalConstants.AuditEventType eventType,
         final T eventData) {

      final var auditEvent = new AuditEvent(
            new Timestamp(System.currentTimeMillis()),
            null,
            eventType,
            getSerializedEventData(eventData)
      );

      try {
         final var json = OBJECT_MAPPER.writeValueAsString(auditEvent);
         LOGGER.info(json);
      } catch (JsonProcessingException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }

      topicAuditEvents.produceAsync(UUID.randomUUID().toString(),
                                    auditEvent,
                                    (metadata, exception) -> {
                                       if (exception != null) {
                                          LOGGER.error(exception.getMessage(), exception);
                                       }
                                    });
   }

}
