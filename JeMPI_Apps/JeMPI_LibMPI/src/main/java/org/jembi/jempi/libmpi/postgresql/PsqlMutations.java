package org.jembi.jempi.libmpi.postgresql;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiException;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.shared.config.Config;
import org.jembi.jempi.shared.models.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

final class PsqlMutations {

   private static final Logger LOGGER = LogManager.getLogger(PsqlMutations.class);
   private static final GoldenRecordDAO GOLDEN_RECORD_DAO = new GoldenRecordDAO();
   private static final SourceIdDAO SOURCE_ID_DAO = new SourceIdDAO();
   private static final EncounterDAO ENCOUNTER_DAO = new EncounterDAO();

   private PsqlMutations() {
   }

   private static UUID insertGoldenRecord(
         final PsqlClient psqlClient,
         final Interaction interaction) throws SQLException, MpiException {
      final var sqlGoldenRecord = new GoldenRecordDAO.SqlGoldenRecord(
            null,
            interaction.demographicData().fields.get(0).value(),
            interaction.demographicData().fields.get(1).value(),
            interaction.demographicData().fields.get(2).value(),
            interaction.demographicData().fields.get(3).value(),
            interaction.demographicData().fields.get(4).value(),
            interaction.demographicData().fields.get(5).value(),
            interaction.demographicData().fields.get(6).value(),
            interaction.demographicData().fields.get(7).value(),
            interaction.demographicData().fields.get(8).value(),
            interaction.demographicData().fields.get(9).value(),
            interaction.demographicData().fields.get(10).value(),
            interaction.demographicData().fields.get(11).value(),
            LocalDateTime.now(),
            true,
            interaction.auxInteractionData().auxUserFields().getFirst().value());
      psqlClient.connect();
      return GOLDEN_RECORD_DAO.insert(psqlClient, sqlGoldenRecord);
   }

   private static UUID insertSourceId(
         final PsqlClient psqlClient,
         final SourceId sourceId,
         final UUID goldenId) throws SQLException, MpiException {
      final var sqlSourceId = new SourceIdDAO.SqlSourceId(null,
                                                          sourceId.facility(),
                                                          sourceId.patient(),
                                                          goldenId);
      psqlClient.connect();
      return SOURCE_ID_DAO.insert(psqlClient, sqlSourceId);
   }

   private static UUID insertEncounter(
         final PsqlClient psqlClient,
         final Interaction interaction,
         final UUID goldenId,
         final float score,
         final UUID sourceId) throws SQLException, MpiException {
      final var sqlEncounter = new EncounterDAO.SqlEncounter(
            null,
            interaction.demographicData().fields.get(0).value(),
            interaction.demographicData().fields.get(1).value(),
            interaction.demographicData().fields.get(2).value(),
            interaction.demographicData().fields.get(3).value(),
            interaction.demographicData().fields.get(4).value(),
            interaction.demographicData().fields.get(5).value(),
            interaction.demographicData().fields.get(6).value(),
            interaction.demographicData().fields.get(7).value(),
            interaction.demographicData().fields.get(8).value(),
            interaction.demographicData().fields.get(9).value(),
            interaction.demographicData().fields.get(10).value(),
            interaction.demographicData().fields.get(11).value(),
            goldenId,
            score,
            sourceId,
            interaction.auxInteractionData().auxDateCreated(),
            interaction.auxInteractionData().auxUserFields().getFirst().value());
      psqlClient.connect();
      return ENCOUNTER_DAO.insert(psqlClient, sqlEncounter);
   }

   static Option<MpiGeneralError> setScore(
         final PsqlClient psqlClient,
         final String interactionUID,
         final String goldenRecordUid,
         final Float score) throws SQLException, MpiException {
      psqlClient.connect();
      if (ENCOUNTER_DAO.updateScore(psqlClient,
                                    UUID.fromString(interactionUID),
                                    UUID.fromString(goldenRecordUid),
                                    score)) {
         return Option.none();
      }
      LOGGER.error("Set score failed: {} --> {} {}", interactionUID, goldenRecordUid, score);
      return Option.of(new MpiServiceError.InternalError("Cound not set score"));
   }

   static void updateField(
         final PsqlClient psqlClient,
         final String goldenRecordUid,
         final String ccField,
         final String value) throws SQLException, MpiException {
      psqlClient.connect();
      GOLDEN_RECORD_DAO.setFieldStringValueById(psqlClient,
                                                UUID.fromString(goldenRecordUid),
                                                ccField,
                                                value);
   }

   static LinkInfo createInteractionAndLinkToExistingGoldenRecord(
         final PsqlClient psqlClient,
         final Interaction interaction,
         final LibMPIClientInterface.GoldenIdScore goldenIdScore) throws SQLException, MpiException {

      final var sourceId = SOURCE_ID_DAO.getByFacilityCodePatientId(psqlClient,
                                                              interaction.sourceId().facility(),
                                                              interaction.sourceId().patient());
      final UUID sourceUuid;
      if (sourceId != null) {
         if (sourceId.goldenRecordUid().compareTo(UUID.fromString(goldenIdScore.goldenId())) != 0) {
            LOGGER.error("REFERENTIAL INTEGRITY: {} {} {} {}",  sourceId.goldenRecordUid().toString(),
                         goldenIdScore.goldenId(),
                         interaction.sourceId().facility(),
                         interaction.sourceId().patient());
         }
         sourceUuid = sourceId.uid();
      } else {
         sourceUuid = insertSourceId(psqlClient, interaction.sourceId(),
                                     UUID.fromString(goldenIdScore.goldenId()));
      }
      final var encounterId = insertEncounter(psqlClient, interaction,
                                              UUID.fromString(goldenIdScore.goldenId()),
                                              goldenIdScore.score(),
                                              sourceUuid);
      return new LinkInfo(goldenIdScore.goldenId(),
                          encounterId.toString(),
                          sourceUuid.toString(),
                          goldenIdScore.score());
   }

   static Either<MpiGeneralError, LinkInfo> createInteractionAndLinkToClonedGoldenRecord(
         final PsqlClient psqlClient,
         final Interaction interaction,
         final Float score) {
      try {
         final var goldenId = insertGoldenRecord(psqlClient, interaction);
         final var sourceId = insertSourceId(psqlClient, interaction.sourceId(), goldenId);
         final var encounterId = insertEncounter(psqlClient, interaction, goldenId, score, sourceId);
         final var linkInfo = new LinkInfo(goldenId.toString(),
                                           encounterId.toString(),
                                           sourceId.toString(),
                                           score);
         return Either.right(linkInfo);
      } catch (SQLException | MpiException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Either.left(new MpiServiceError.InternalError(e.getLocalizedMessage()));
      }
   }

   static Either<MpiGeneralError, LinkInfo> linkToNewGoldenRecord(
         final PsqlClient psqlClient,
         final String currentGoldenId,
         final String interactionId,
         final Float score) {
      LOGGER.debug("linkToNewGoldenRecord");
      try {
         final var sqlEncounter = ENCOUNTER_DAO.getById(psqlClient, UUID.fromString(interactionId));
         final var sqlSourceId = SOURCE_ID_DAO.getById(psqlClient, sqlEncounter.sourceIdUid());
         final var interaction = ENCOUNTER_DAO.mapToInteraction(sqlEncounter, sqlSourceId);
         final var newGoldenId = insertGoldenRecord(psqlClient, interaction);
         ENCOUNTER_DAO.setFieldUuidValueById(psqlClient,
                                             UUID.fromString(interactionId),
                                             "goldenRecordUid",
                                             newGoldenId);
         SOURCE_ID_DAO.setFieldUuidValueById(psqlClient,
                                             sqlEncounter.sourceIdUid(),
                                             "goldenRecordUid",
                                             newGoldenId);
         setScore(psqlClient, interactionId, newGoldenId.toString(), score);
         final var count = ENCOUNTER_DAO.countEncountersForGoldenId(psqlClient, UUID.fromString(currentGoldenId));
         if (count == 0) {
            GOLDEN_RECORD_DAO.delete(psqlClient, UUID.fromString(currentGoldenId));
         }
         return Either.right(new LinkInfo(newGoldenId.toString(), interactionId, sqlSourceId.uid().toString(), score));
      } catch (SQLException | MpiException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Either.left(new MpiServiceError.InternalError(e.getLocalizedMessage()));
      }
   }

   static LinkInfo updateLink(
         final PsqlClient psqlClient,
         final String goldenId,
         final String newGoldenId,
         final String interactionId,
         final Float score) throws SQLException, MpiException {
      LOGGER.debug("updateLink {}", goldenId);
      LOGGER.debug("updateLink {}", newGoldenId);
      LOGGER.debug("updateLink {}", interactionId);
      LOGGER.debug("updateLink {}", score);
      UUID sourceId;
      psqlClient.connect();
      sourceId = ENCOUNTER_DAO.getFieldUuidValueById(psqlClient, UUID.fromString(interactionId), "sourceIdUid");
      if (!newGoldenId.equals(goldenId)) {
         ENCOUNTER_DAO.setFieldUuidValueById(psqlClient,
                                             UUID.fromString(interactionId),
                                             "goldenRecordUid",
                                             UUID.fromString(newGoldenId));
         SOURCE_ID_DAO.setFieldUuidValueById(psqlClient,
                                             sourceId,
                                             "goldenRecordUid",
                                             UUID.fromString(newGoldenId));
         final var count = ENCOUNTER_DAO.countEncountersForGoldenId(psqlClient, UUID.fromString(goldenId));
         if (count == 0) {
            LOGGER.info("Delete orphaned goldenRecord: {}", goldenId);
            GOLDEN_RECORD_DAO.delete(psqlClient, UUID.fromString(goldenId));
         }
      }
      return new LinkInfo(newGoldenId, interactionId, sourceId.toString(), score);
   }

   static ApiModels.ApiCivilRecordResponse insertCivilRecord(
         final PsqlClient psqlClient,
         final String auxId,
         final DemographicData demographicData) throws MpiException, SQLException {
      try {
         final var sqlGoldenRecord = new GoldenRecordDAO.SqlGoldenRecord(
               null,
               demographicData.fields.get(0).value().toLowerCase(Locale.ROOT),
               demographicData.fields.get(1).value().toLowerCase(Locale.ROOT),
               demographicData.fields.get(2).value().toLowerCase(Locale.ROOT),
               demographicData.fields.get(3).value().toLowerCase(Locale.ROOT),
               demographicData.fields.get(4).value().toLowerCase(Locale.ROOT),
               demographicData.fields.get(5).value().toLowerCase(Locale.ROOT),
               demographicData.fields.get(6).value().toLowerCase(Locale.ROOT),
               demographicData.fields.get(7).value().toLowerCase(Locale.ROOT),
               demographicData.fields.get(8).value().toLowerCase(Locale.ROOT),
               demographicData.fields.get(9).value().toLowerCase(Locale.ROOT),
               demographicData.fields.get(10).value().toLowerCase(Locale.ROOT),
               demographicData.fields.get(11).value().toLowerCase(Locale.ROOT),
               LocalDateTime.now(),
               true,
               auxId);
         psqlClient.connect();
         psqlClient.setAutoCommit(false);
         final var gUuid = GOLDEN_RECORD_DAO.insert(psqlClient, sqlGoldenRecord);
         final var sqlSourceId = new SourceIdDAO.SqlSourceId(
               null,
               "CIVIL",
               demographicData.fields.get(Config.FIELDS_CONFIG.findIndexOfDemographicField("pin")).value(),
               gUuid);
         final var sUuid = SOURCE_ID_DAO.insert(psqlClient, sqlSourceId);
         final var sqlEncounter = new EncounterDAO.SqlEncounter(
               null,
               demographicData.fields.get(0).value().toLowerCase(Locale.ROOT),
               demographicData.fields.get(1).value().toLowerCase(Locale.ROOT),
               demographicData.fields.get(2).value().toLowerCase(Locale.ROOT),
               demographicData.fields.get(3).value().toLowerCase(Locale.ROOT),
               demographicData.fields.get(4).value().toLowerCase(Locale.ROOT),
               demographicData.fields.get(5).value().toLowerCase(Locale.ROOT),
               demographicData.fields.get(6).value().toLowerCase(Locale.ROOT),
               demographicData.fields.get(7).value().toLowerCase(Locale.ROOT),
               demographicData.fields.get(8).value().toLowerCase(Locale.ROOT),
               demographicData.fields.get(9).value().toLowerCase(Locale.ROOT),
               demographicData.fields.get(10).value().toLowerCase(Locale.ROOT),
               demographicData.fields.get(11).value().toLowerCase(Locale.ROOT),
               gUuid,
               1.0F,
               sUuid,
               LocalDateTime.now(),
               auxId);
         final var eUuid = ENCOUNTER_DAO.insert(psqlClient, sqlEncounter);
         psqlClient.commit();
         psqlClient.setAutoCommit(true);
         return new ApiModels.ApiCivilRecordResponse(gUuid.toString(),
                                                     sUuid.toString(),
                                                     eUuid.toString());
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         psqlClient.rollback();
         throw e;
      } finally {
         psqlClient.setAutoCommit(true);
      }
   }

   static void connect(final PsqlClient psqlClient) throws SQLException, MpiException {
      psqlClient.connect();
   }

}
