package org.jembi.jempi.libmpi.postgresql;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiException;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.libmpi.common.PaginatedResultSet;
import org.jembi.jempi.shared.models.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public final class LibPostgreSQL implements LibMPIClientInterface {

   private static final Logger LOGGER = LogManager.getLogger(LibPostgreSQL.class);
   private PsqlClient psqlClient;

   public LibPostgreSQL(final String pgIp,
                        final Integer pgPort,
                        final String pgUser,
                        final String pgPassword,
                        final String pgDb) {
      LOGGER.info("{}", "LibPostgreSQL Constructor");
      LOGGER.info("{}", pgIp);
      psqlClient = new PsqlClient(pgIp, pgPort, pgUser, pgPassword, pgDb);
      connect();
   }

   @Override
   public Option<MpiGeneralError> connect() {
      try {
         PsqlQueries.connect(psqlClient);
         PsqlMutations.connect(psqlClient);
         return Option.none();
      } catch (SQLException | MpiException e) {
         LOGGER.error(e.getMessage(), e);
         return Option.of(new MpiServiceError.InternalError(e.getMessage()));
      }
   }

   @Override
   public Option<MpiGeneralError> dropAll() {
      LOGGER.error("LibPostgreSQL dropAll error");
      return Option.of(new MpiServiceError.NotImplementedError("dropAll"));
   }

   @Override
   public Option<MpiGeneralError> dropAllData() {
      LOGGER.error("LibPostgreSQL dropAllData error");
      return Option.of(new MpiServiceError.NotImplementedError("dropAllData"));
   }

   @Override
   public Option<MpiGeneralError> createSchema() {
      LOGGER.error("LibPostgreSQL createSchema error");
      return Option.of(new MpiServiceError.NotImplementedError("createSchema"));
   }

   @Override
   public Either<MpiGeneralError, Long> countInteractions() {
      return PsqlQueries.countInteractions(psqlClient);
   }

   @Override
   public Either<MpiGeneralError, Long> countGoldenRecords() {
      return PsqlQueries.countGoldenRecords(psqlClient);
   }

   @Override
   public Either<MpiGeneralError, List<SourceId>> findSourceId(
         final String facility,
         final String client) {
      LOGGER.error("LibPostgreSQL findSourceId error");
      return Either.left(new MpiServiceError.NotImplementedError("findSourceId"));
   }

   @Override
   public Either<MpiGeneralError, List<ExpandedSourceId>> findExpandedSourceIdList(
         final String facility,
         final String client) {
      LOGGER.error("LibPostgreSQL findExpandedSourceIdList error");
      return Either.left(new MpiServiceError.NotImplementedError("findExpandedSourceIdList"));
   }

   @Override
   public Either<MpiGeneralError, Interaction> findInteraction(final String interactionID) {
      LOGGER.error("LibPostgreSQL findInteraction error");
      return Either.left(new MpiServiceError.NotImplementedError("findInteraction"));
   }

   @Override
   public Either<MpiGeneralError, List<Interaction>> findInteractions(final List<String> interactionIDs) {
      LOGGER.error("LibPostgreSQL findInteractions error");
      return Either.left(new MpiServiceError.NotImplementedError("findInetractions"));
   }

   @Override
   public Either<MpiGeneralError, List<ExpandedInteraction>> findExpandedInteractions(final List<String> interactionIDs) {
      LOGGER.debug("findExpandedInteractions");
      return PsqlQueries.findExpandedInteractions(psqlClient, interactionIDs);
   }

   @Override
   public Either<MpiGeneralError, PaginatedResultSet<GoldenRecord>> findGoldenRecords(final List<String> goldenIds) {
      LOGGER.error("LibPostgreSQL findGoldenRecords error");
      return Either.left(new MpiServiceError.NotImplementedError("findGoldenRecords"));
   }

   @Override
   public Either<MpiGeneralError, PaginatedResultSet<ExpandedGoldenRecord>> findExpandedGoldenRecords(final List<String> goldenIds) {
      LOGGER.debug("findExpandedGoldenRecords");
      return PsqlQueries.findExpandedGoldenRecords(psqlClient, goldenIds);
   }

   @Override
   public Either<MpiGeneralError, List<String>> findGoldenIds() {
      LOGGER.debug("findGoldenIds");
      return PsqlQueries.findGoldenIds(psqlClient);
   }

   @Override
   public Either<MpiGeneralError, List<String>> fetchGoldenIds(
         final long offset,
         final long length) {
      LOGGER.error("LibPostgreSQL fetchGoldenIds error");
      return Either.left(new MpiServiceError.NotImplementedError("fetchGoldenIds"));
   }

   @Override
   public Either<MpiGeneralError, List<GoldenRecord>> findLinkCandidates(final DemographicData demographicData) {
      LOGGER.debug("findLinkCandidates");
      return PsqlQueries.findLinkCandidates(psqlClient, demographicData);
   }

   @Override
   public Either<MpiGeneralError, String> restoreGoldenRecord(final RestoreGoldenRecords goldenRecord) {
      LOGGER.error("LibPostgreSQL restoreGoldenRecord error");
      return Either.left(new MpiServiceError.NotImplementedError("restoreGoldenRecord"));
   }

   @Override
   public Either<MpiGeneralError, List<GoldenRecord>> findMatchCandidates(final DemographicData demographicData) {
      LOGGER.error("LibPostgreSQL findMatchCandidates error");
      return Either.left(new MpiServiceError.NotImplementedError("findMatchCandidates"));
   }

   @Override
   public Either<MpiGeneralError, PaginatedResultSet<ExpandedGoldenRecord>> simpleSearchGoldenRecords(
         final List<ApiModels.ApiSearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      LOGGER.debug("simpleSearchGoldenRecords");
      return PsqlQueries.simpleSearchGoldenRecords(psqlClient, params, offset, limit, sortBy, sortAsc);
   }

   @Override
   public Either<MpiGeneralError, PaginatedResultSet<ExpandedGoldenRecord>> customSearchGoldenRecords(
         final List<ApiModels.ApiSimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      LOGGER.error("LibPostgreSQL customSearchGoldenRecords error");
      return Either.left(new MpiServiceError.NotImplementedError("customSearchGoldenRecords"));
   }

   @Override
   public Either<MpiGeneralError, PaginatedResultSet<Interaction>> simpleSearchInteractions(
         final List<ApiModels.ApiSearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      LOGGER.error("LibPostgreSQL simpleSearchInteractions error");
      return Either.left(new MpiServiceError.NotImplementedError("simpleSearchInteractions"));
   }

   @Override
   public Either<MpiGeneralError, PaginatedResultSet<Interaction>> customSearchInteractions(
         final List<ApiModels.ApiSimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      LOGGER.error("LibPostgreSQL customSearchInteractions error");
      return Either.left(new MpiServiceError.NotImplementedError("customSearchInteractions"));
   }

   @Override
   public Either<MpiGeneralError, LibMPIPaginatedResultSet<String>> filterGids(
         final List<ApiModels.ApiSearchParameter> params,
         final LocalDateTime createdAt,
         final PaginationOptions paginationOptions) {
      LOGGER.error("LibPostgreSQL filterGids error");
      return Either.left(new MpiServiceError.NotImplementedError("filterGids"));
   }

   @Override
   public Either<MpiGeneralError, PaginatedGIDsWithInteractionCount> filterGidsWithInteractionCount(
         final List<ApiModels.ApiSearchParameter> params,
         final LocalDateTime createdAt,
         final PaginationOptions paginationOptions) {
      LOGGER.error("LibPostgreSQL filterGidsWithInteractionCount error");
      return Either.left(new MpiServiceError.NotImplementedError("filterGidsWithInteractionCount"));
   }

   @Override
   public Either<MpiGeneralError, PaginatedResultSet<GoldenRecord>> apiCrFindGoldenRecords(final ApiModels.ApiCrFindRequest request) {
      LOGGER.error("LibPostgreSQL apiCrFindGoldenRecords error");
      return Either.left(new MpiServiceError.NotImplementedError("apiCrFindGoldenRecords"));
   }

   @Override
   public Either<MpiGeneralError, ApiModels.ApiCivilRecordResponse> insertCivilRecord(
         final String auxId,
         final DemographicData demographicData) {
      try {
         final var result = PsqlMutations.insertCivilRecord(psqlClient, auxId, demographicData);
         return Either.right(result);
      } catch (SQLException | MpiException e) {
         LOGGER.error(e.getMessage(), e);
         return Either.left(new MpiServiceError.InternalError(e.getMessage()));
      }
   }

   @Override
   public Option<MpiGeneralError> setScore(
         final String interactionUID,
         final String goldenRecordUid,
         final Float score) {
      try {
         PsqlMutations.setScore(psqlClient, interactionUID, goldenRecordUid, score);
         return Option.none();
      } catch (SQLException | MpiException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Option.of(new MpiServiceError.InternalError(e.getLocalizedMessage()));
      }
   }

   @Override
   public Option<MpiGeneralError> updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final String value) {
      try {
         PsqlMutations.updateField(psqlClient, goldenId, fieldName, value);
         return Option.none();
      } catch (SQLException | MpiException e) {
         return Option.of(new MpiServiceError.InternalError(e.getMessage()));
      }
   }

   @Override
   public Option<MpiGeneralError> updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final Boolean value) {
      LOGGER.error("LibPostgreSQL updateGoldenRecordField error");
      return Option.of(new MpiServiceError.NotImplementedError("updateGoldenRecordField"));
   }

   @Override
   public Option<MpiGeneralError> updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final Double value) {
      LOGGER.error("LibPostgreSQL updateGoldenRecordField error");
      return Option.of(new MpiServiceError.NotImplementedError("updateGoldenRecordField"));
   }

   @Override
   public Option<MpiGeneralError> updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final Long value) {
      LOGGER.error("LibPostgreSQL updateGoldenRecordField error");
      return Option.of(new MpiServiceError.NotImplementedError("updateGoldenRecordField"));
   }

   @Override
   public Either<MpiGeneralError, LinkInfo> linkToNewGoldenRecord(
         final String currentGoldenId,
         final String interactionId,
         final Float score) {
      return PsqlMutations.linkToNewGoldenRecord(psqlClient, currentGoldenId, interactionId, score);
   }

   @Override
   public Either<MpiGeneralError, LinkInfo> updateLink(
         final String goldenId,
         final String newGoldenId,
         final String interactionId,
         final Float score) {
      try {
         final var result = PsqlMutations.updateLink(psqlClient, goldenId, newGoldenId, interactionId, score);
         return Either.right(result);
      } catch (SQLException | MpiException e) {
         LOGGER.error(e.getMessage(), e);
         return Either.left(new MpiServiceError.InternalError(e.getMessage()));
      }
   }

   @Override
   public Either<MpiGeneralError, LinkInfo> createInteractionAndLinkToExistingGoldenRecord(
         final Interaction interaction,
         final GoldenIdScore goldenIdScore) {
      try {
         final var result = PsqlMutations.createInteractionAndLinkToExistingGoldenRecord(psqlClient, interaction, goldenIdScore);
         return Either.right(result);
      } catch (SQLException | MpiException e) {
         LOGGER.error(e.getMessage(), e);
         return Either.left(new MpiServiceError.InternalError(e.getMessage()));
      }
   }

   @Override
   public Either<MpiGeneralError, LinkInfo> createInteractionAndLinkToClonedGoldenRecord(
         final Interaction interaction,
         final Float score) {
      return PsqlMutations.createInteractionAndLinkToClonedGoldenRecord(psqlClient, interaction, score);
   }

}
