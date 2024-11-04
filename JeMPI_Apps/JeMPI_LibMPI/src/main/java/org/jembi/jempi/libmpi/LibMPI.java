package org.jembi.jempi.libmpi;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.postgresql.LibPostgreSQL;
import org.jembi.jempi.shared.kafka.MyKafkaProducer;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.serdes.JsonPojoSerializer;
import org.jembi.jempi.shared.utils.AuditTrailBridge;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public final class LibMPI {

   private static final Logger LOGGER = LogManager.getLogger(LibMPI.class);
   private final LibMPIClientInterface client;
   private final MyKafkaProducer<String, AuditEvent> topicAuditEvents;
   private final MyKafkaProducer<String, Validation> topicValidation;
   private final MyKafkaProducer<String, MpiMediator> topicMpiMediator;
   private final AuditTrailBridge auditTrailUtil;

   public LibMPI(
         final Level level,
         final String[] host,
         final int[] port,
         final String kafkaBootstrapServers,
         final String kafkaClientId,
         final PgConfig pgConfig) {
      LOGGER.info("{}", "LibMPI Constructor");
      topicAuditEvents = new MyKafkaProducer<>(kafkaBootstrapServers,
                                               GlobalConstants.TOPIC_AUDIT_TRAIL,
                                               new StringSerializer(),
                                               new JsonPojoSerializer<>(),
                                               kafkaClientId);
      auditTrailUtil = new AuditTrailBridge(topicAuditEvents);
      topicValidation = new MyKafkaProducer<>(kafkaBootstrapServers,
                                              GlobalConstants.TOPIC_INTERACTION_VALIDATE,
                                              new StringSerializer(),
                                              new JsonPojoSerializer<>(),
                                              kafkaClientId);
      topicMpiMediator = new MyKafkaProducer<>(kafkaBootstrapServers,
                                               GlobalConstants.TOPIC_MPI_MEDIATOR,
                                               new StringSerializer(),
                                               new JsonPojoSerializer<>(),
                                               kafkaClientId);

//      client = new LibDgraph(level, host, port);
      client = new LibPostgreSQL(pgConfig.pgIp, pgConfig.pgPort, pgConfig.pgUser, pgConfig.pgPassword, pgConfig.pgDb);
      client.connect();
   }

   private void sendAuditEvent(
         final String interactionID,
         final String goldenID,
         final String message,
         final float score,
         final LinkingRule linkingRule) {

      LinkingAuditEventData linkingEvent = new LinkingAuditEventData(message, interactionID, goldenID, score, linkingRule);
      auditTrailUtil.sendAuditEvent(GlobalConstants.AuditEventType.LINKING_EVENT, linkingEvent);

      final var event = switch (linkingRule) {
         case NEW, DETERMINISTIC, PROBABILISTIC, EXPLICIT_GID, EXPLICIT_SOURCE_ID -> "Interaction -> new GoldenID";
         case NOTIFICATION -> "Interaction -> update GoldenID";
         case UPDATE -> null;
      };
      if (event != null) {
         try {
            final var mpiMediatorMessage = new MpiMediator(LocalDateTime.now(),
                                                           interactionID,
                                                           goldenID,
                                                           event);
            topicMpiMediator.produceSync(interactionID, mpiMediatorMessage);
         } catch (ExecutionException | InterruptedException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
         }
      } else {
         LOGGER.debug("{} {}", linkingRule, message);
      }
   }

   public Option<MpiGeneralError> dropAll() {
      client.connect();
      return client.dropAll();
   }


   /*
    * *****************************************************************************
    * *
    * Database
    * *****************************************************************************
    * *
    */

   public Option<MpiGeneralError> dropAllData() {
      return client.dropAllData();
   }

   public Option<MpiGeneralError> createSchema() {
      return client.createSchema();
   }

   public Either<MpiGeneralError, Long> countInteractions() {
      client.connect();
      return client.countInteractions();
   }

   /*
    * *****************************************************************************
    * *
    * Queries
    * *****************************************************************************
    * *
    */

   public Either<MpiGeneralError, Long> countGoldenRecords() {
      client.connect();
      return client.countGoldenRecords();
   }

   public Either<MpiGeneralError, List<SourceId>> findSourceId(
         final String facility,
         final String patient) {
      client.connect();
      return client.findSourceId(facility, patient);
   }

   public Either<MpiGeneralError, List<ExpandedSourceId>> findExpandedSourceIdList(
         final String facility,
         final String patient) {
      client.connect();
      return client.findExpandedSourceIdList(facility, patient);
   }

   public Either<MpiGeneralError, Interaction> findInteraction(final String iid) {
      client.connect();
      return client.findInteraction(iid);
   }

   public Either<MpiGeneralError, List<Interaction>> findInteractions(final List<String> iidList) {
      client.connect();
      return client.findInteractions(iidList);
   }

   public Either<MpiGeneralError, List<ExpandedInteraction>> findExpandedInteractions(final List<String> interactionIDs) {
      client.connect();
      return client.findExpandedInteractions(interactionIDs);
   }

   public Either<MpiGeneralError, GoldenRecord> findGoldenRecord(final String goldenId) {
      client.connect();
      final var records = client.findGoldenRecords(List.of(goldenId));
      if (records.isRight()) {
         if (!records.get().data().isEmpty()) {
            return Either.right(records.get().data().getFirst());
         } else {
            return Either.left(new MpiServiceError.CRGidDoesNotExistError(goldenId));
         }
      }
      return Either.left(records.getLeft());
   }

   public Either<MpiGeneralError, List<GoldenRecord>> findGoldenRecords(final List<String> goldenIds) {
      client.connect();
      final var results = client.findGoldenRecords(goldenIds);
      if (results.isLeft()) {
         return Either.left(results.getLeft());
      }
      return Either.right(results.get().data());
   }

   public Either<MpiGeneralError, ExpandedGoldenRecord> findExpandedGoldenRecord(final String goldenId) {
      client.connect();
      final var results = client.findExpandedGoldenRecords(List.of(goldenId));
      if (results.isLeft()) {
         return Either.left(results.getLeft());
      }
      if (!results.get().data().isEmpty()) {
         return Either.right(results.get().data().getFirst());
      }
      return Either.right(null);
   }

   public Either<MpiGeneralError, List<ExpandedGoldenRecord>> findExpandedGoldenRecords(final List<String> goldenIds) {
      client.connect();
      final var results = client.findExpandedGoldenRecords(goldenIds);
      if (results.isLeft()) {
         return Either.left(results.getLeft());
      }
      return Either.right(client.findExpandedGoldenRecords(goldenIds).get().data());
   }

   public Either<MpiGeneralError, String> postGoldenRecord(final RestoreGoldenRecords goldenRecord) {
      client.connect();
      return client.restoreGoldenRecord(goldenRecord);
   }

   public Either<MpiGeneralError, List<String>> findGoldenIds() {
      client.connect();
      return client.findGoldenIds();
   }

   public Either<MpiGeneralError, List<String>> fetchGoldenIds(
         final long offset,
         final long length) {
      client.connect();
      return client.fetchGoldenIds(offset, length);
   }

   public Either<MpiGeneralError, List<GoldenRecord>> findLinkCandidates(final DemographicData demographicData) {
      client.connect();
      return client.findLinkCandidates(demographicData);
   }

   public Either<MpiGeneralError, List<GoldenRecord>> findMatchCandidates(final DemographicData demographicData) {
      client.connect();
      return client.findMatchCandidates(demographicData);
   }

   public Either<MpiGeneralError, List<GoldenRecord>> apiCrFindGoldenRecords(final ApiModels.ApiCrFindRequest request) {
      client.connect();
      final var results = client.apiCrFindGoldenRecords(request);
      if (results.isLeft()) {
         return Either.left(results.getLeft());
      }
      return Either.right(results.get().data());
   }

   public Either<MpiGeneralError, LibMPIPaginatedResultSet<ExpandedGoldenRecord>> simpleSearchGoldenRecords(
         final List<ApiModels.ApiSearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      client.connect();
      final var results = client.simpleSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
      return Either.right(new LibMPIPaginatedResultSet<>(results.get().data(), results.get().pagination().getFirst()));
   }

   public Either<MpiGeneralError, LibMPIPaginatedResultSet<ExpandedGoldenRecord>> customSearchGoldenRecords(
         final List<ApiModels.ApiSimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      client.connect();
      final var results = client.customSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
      return Either.right(new LibMPIPaginatedResultSet<>(results.get().data(), results.get().pagination().getFirst()));
   }

   public Either<MpiGeneralError, LibMPIPaginatedResultSet<Interaction>> simpleSearchInteractions(
         final List<ApiModels.ApiSearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      client.connect();
      final var results = client.simpleSearchInteractions(params, offset, limit, sortBy, sortAsc);
      return Either.right(new LibMPIPaginatedResultSet<>(results.get().data(), results.get().pagination().getFirst()));
   }

   public Either<MpiGeneralError, LibMPIPaginatedResultSet<Interaction>> customSearchInteractions(
         final List<ApiModels.ApiSimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      client.connect();
      final var results = client.customSearchInteractions(params, offset, limit, sortBy, sortAsc);
      return Either.right(new LibMPIPaginatedResultSet<>(results.get().data(), results.get().pagination().getFirst()));
   }

   public Either<MpiGeneralError, LibMPIPaginatedResultSet<String>> filterGids(
         final List<ApiModels.ApiSearchParameter> params,
         final LocalDateTime createdAt,
         final PaginationOptions paginationOptions) {
      client.connect();
      return client.filterGids(params, createdAt, paginationOptions);
   }

   public Either<MpiGeneralError, PaginatedGIDsWithInteractionCount> filterGidsWithInteractionCount(
         final List<ApiModels.ApiSearchParameter> params,
         final LocalDateTime createdAt,
         final PaginationOptions paginationOptions) {
      client.connect();
      return client.filterGidsWithInteractionCount(params, createdAt, paginationOptions);
   }

   public Either<MpiGeneralError, ApiModels.ApiCivilRecordResponse> insertCivilRecord(
         final String auxId,
         final DemographicData demographicData) {
      return client.insertCivilRecord(auxId, demographicData);
   }

   /*
    * *****************************************************************************
    * *
    * Mutations
    * *****************************************************************************
    * *
    */

   public Option<MpiGeneralError> setScore(
         final String interactionID,
         final String goldenID,
         final float oldScore,
         final float newScore) {
      final var connectError = client.connect();
      if (connectError.isDefined()) {
         return connectError;
      }
      final var result = client.setScore(interactionID, goldenID, newScore);
      if (result.isEmpty()) {
         sendAuditEvent(interactionID,
                        goldenID,
                        "score: %.5f -> %.5f".formatted(oldScore, newScore),
                        newScore,
                        LinkingRule.UPDATE);
      } else {
         sendAuditEvent(interactionID,
                        goldenID,
                        "set score error: %.5f -> %.5f".formatted(oldScore, newScore),
                        newScore,
                        LinkingRule.UPDATE);

      }
      return result;
   }

   public Option<MpiGeneralError> updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final String newValue) {
      final var connectError = client.connect();
      if (connectError.isDefined()) {
         return connectError;
      }
      final var updateError = client.updateGoldenRecordField(goldenId, fieldName, newValue);
      if (updateError.isEmpty()) {
         sendAuditEvent(null,
                        goldenId,
                        "Update Golden Record Field: %s -> %s".formatted(fieldName, newValue),
                        -1.0F,
                        LinkingRule.UPDATE);
      }
      return updateError;
   }

   public Option<MpiGeneralError> updateGoldenRecordField(
         final String interactionId,
         final String goldenId,
         final String fieldName,
         final String oldValue,
         final String newValue,
         final String alias) {
      final var connectError = client.connect();
      if (connectError.isDefined()) {
         return connectError;
      }
      final var updateError = client.updateGoldenRecordField(goldenId, fieldName, newValue);
      if (updateError.isEmpty()) {
         sendAuditEvent(interactionId, goldenId, "%s: '%s' -> '%s'".formatted(alias, oldValue, newValue),
                        -1.0F,
                        LinkingRule.UPDATE);
      } else {
         sendAuditEvent(interactionId, goldenId, "%s: error updating '%s' -> '%s'".formatted(alias, oldValue, newValue),
                        -1.0F,
                        LinkingRule.UPDATE);
      }
      return updateError;
   }

   public Either<MpiGeneralError, LinkInfo> linkToNewGoldenRecord(
         final String currentGoldenId,
         final String interactionId,
         final Float score) {
      final var connectError = client.connect();
      if (connectError.isDefined()) {
         return Either.left(connectError.get());
      }
      if (score == null) {
         LOGGER.error("Missing score");
         return Either.left(new MpiServiceError.NoScoreGivenError("Missing Score"));
      }
      final var result = client.linkToNewGoldenRecord(currentGoldenId, interactionId, score);
      if (result.isRight()) {
         sendAuditEvent(interactionId,
                        result.get().goldenUID(),
                        String.format(Locale.ROOT,
                                      "Interaction -> new GoldenID: old(%s) new(%s) [%f]",
                                      currentGoldenId,
                                      result.get().goldenUID(),
                                      score), score, LinkingRule.NOTIFICATION);
      } else {
         sendAuditEvent(interactionId,
                        currentGoldenId,
                        String.format(Locale.ROOT,
                                      "Interaction -> update GoldenID error: old(%s) [%f]",
                                      currentGoldenId, score),
                        score,
                        LinkingRule.NOTIFICATION);
      }
      return result;
   }

   public Either<MpiGeneralError, LinkInfo> updateLink(
         final String goldenID,
         final String newGoldenID,
         final String interactionID,
         final Float score) {
      final var connectError = client.connect();
      if (connectError.isDefined()) {
         return Either.left(connectError.get());
      }
      if (score == null) {
         LOGGER.error("No score");
         return Either.left(new MpiServiceError.NoScoreGivenError("No score"));
      }
      final var result = client.updateLink(goldenID, newGoldenID, interactionID, score);

      if (result.isRight()) {
         sendAuditEvent(interactionID,
                        newGoldenID,
                        String.format(Locale.ROOT,
                                      "Interaction -> update GoldenID: old(%s) new(%s) [%f]",
                                      goldenID,
                                      newGoldenID,
                                      score), score, LinkingRule.NOTIFICATION);
      } else {
         sendAuditEvent(interactionID,
                        newGoldenID,
                        String.format(Locale.ROOT,
                                      "Interaction -> update GoldenID error: old(%s) new(%s) [%f]",
                                      goldenID,
                                      newGoldenID,
                                      score), score, LinkingRule.NOTIFICATION);
      }
      return result;
   }

   public Either<MpiGeneralError, LinkInfo> createInteractionAndLinkToExistingGoldenRecord(
         final Interaction interaction,
         final LibMPIClientInterface.GoldenIdScore goldenIdScore,
         final boolean deterministicValidation,
         final float probabilisticValidation,
         final LinkingRule linkingRule) {
      final var connectError = client.connect();
      if (connectError.isDefined()) {
         return Either.left(connectError.get());
      }
      final var result = client.createInteractionAndLinkToExistingGoldenRecord(interaction, goldenIdScore);
      if (result.isRight()) {
         sendAuditEvent(result.get().interactionUID(),
                        result.get().goldenUID(),
                        String.format(Locale.ROOT,
                                      "Interaction -> Existing GoldenRecord (%.5f)  /  Validation: Deterministic(%s), "
                                      + "Probabilistic(%.3f)",
                                      result.get().score(),
                                      deterministicValidation,
                                      probabilisticValidation), result.get().score(), linkingRule);

         topicValidation.produceAsync(UUID.randomUUID().toString(),
                                      new Validation(result.get().interactionUID(),
                                                     result.get().goldenUID(),
                                                     deterministicValidation,
                                                     probabilisticValidation),
                                      (metadata, exception) -> {
                                         if (exception != null) {
                                            LOGGER.error(exception.getMessage(), exception);
                                         }
                                      });
      } else {
         sendAuditEvent(interaction.interactionId(),
                        goldenIdScore.goldenId(),
                        String.format(Locale.ROOT,
                                      "Interaction -> error linking to existing GoldenRecord (%.5f)",
                                      goldenIdScore.score()), goldenIdScore.score(), linkingRule);
      }
      return result;
   }

   public Either<MpiGeneralError, LinkInfo> createInteractionAndLinkToClonedGoldenRecord(
         final Interaction interaction,
         final float score) {
      final var connectError = client.connect();
      if (connectError.isDefined()) {
         return Either.left(connectError.get());
      }
      final var result = client.createInteractionAndLinkToClonedGoldenRecord(interaction, score);
      if (result.isRight()) {
         sendAuditEvent(result.get().interactionUID(),
                        result.get().goldenUID(),
                        String.format(Locale.ROOT,
                                      "Interaction -> New GoldenRecord (%f)", score),
                        score, LinkingRule.NEW);
      } else {
         sendAuditEvent(interaction.interactionId(),
                        null,
                        String.format(Locale.ROOT,
                                      "Interaction -> error linking to new GoldenRecord (%f)", score),
                        score, LinkingRule.NEW);
      }
      return result;
   }

   public void sendUpdatedNotificationEvent(
         final String notificationId,
         final String oldGoldenId,
         final String currentGoldenId) {
      final var message = String.format(
            "Notification -> new GoldenID: old(%s) new(%s), new State: old(OPEN) new(CLOSED)",
            oldGoldenId,
            currentGoldenId);
      final var eventData = new NotificationAuditEventData(message, notificationId);
      auditTrailUtil.sendAuditEvent(GlobalConstants.AuditEventType.NOTIFICATION_EVENT, eventData);
   }

   /*
    * *****************************************************************************
    * *
    * Notifications
    * *****************************************************************************
    * *
    */

   public record PgConfig(
         String pgIp,
         Integer pgPort,
         String pgUser,
         String pgPassword,
         String pgDb) {
   }

}
