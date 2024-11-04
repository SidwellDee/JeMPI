package org.jembi.jempi.libmpi.dgraph;

import io.dgraph.DgraphProto;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.LibMPIClientInterface;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.libmpi.common.PaginatedResultSet;
import org.jembi.jempi.shared.models.*;

import java.time.LocalDateTime;
import java.util.List;

import static io.dgraph.DgraphProto.Operation.DropOp.DATA;

public final class LibDgraph implements LibMPIClientInterface {

   private static final Logger LOGGER = LogManager.getLogger(LibDgraph.class);

   private final DgraphMutations dgraphMutations;

   public LibDgraph(
         final Level level,
         final String[] host,
         final int[] port) {
      LOGGER.info("{}", "LibDgraph Constructor");
      LOGGER.info("{} {}", host, port);
      dgraphMutations = new DgraphMutations(level);
      DgraphClient.getInstance().config(host, port);
   }

   /*
    * *******************************************************
    * QUERIES
    * *******************************************************
    *
    */

   public Either<MpiGeneralError, Long> countInteractions() {
      return Either.right(DgraphQueries.countInteractions());
   }

   public Either<MpiGeneralError, Long> countGoldenRecords() {
      return Either.right(DgraphQueries.countGoldenRecords());
   }

   public Either<MpiGeneralError, Interaction> findInteraction(final String interactionId) {
      return Either.right(DgraphQueries.findInteraction(interactionId));
   }

   public Either<MpiGeneralError, List<Interaction>> findInteractions(final List<String> interactionIds) {
      return Either.left(new MpiServiceError.NotImplementedError("findInteractions"));
   }

   public Either<MpiGeneralError, List<SourceId>> findSourceId(
         final String facility,
         final String patient) {
      return Either.right(DgraphQueries.findSourceIdList(facility, patient));
   }

   public Either<MpiGeneralError, List<ExpandedSourceId>> findExpandedSourceIdList(
         final String facility,
         final String patient) {
      return Either.right(DgraphQueries.findExpandedSourceIdList(facility, patient));
   }

   public Either<MpiGeneralError, List<ExpandedInteraction>> findExpandedInteractions(final List<String> interactionIds) {
      return Either.right(DgraphQueries.findExpandedInteractions(interactionIds));
   }

   public Either<MpiGeneralError, PaginatedResultSet<GoldenRecord>> findGoldenRecords(final List<String> ids) {
      return DgraphQueries.findGoldenRecords(ids);
   }

   public Either<MpiGeneralError, PaginatedResultSet<ExpandedGoldenRecord>> findExpandedGoldenRecords(final List<String> goldenIds) {
      return Either.right(DgraphQueries.getExpandedGoldenRecords(goldenIds));
   }

   public Either<MpiGeneralError, String> restoreGoldenRecord(
         final RestoreGoldenRecords goldenRecord) {
      return Either.right(dgraphMutations.restoreGoldenRecord(goldenRecord));
   }

   public Either<MpiGeneralError, List<String>> findGoldenIds() {
      return Either.right(DgraphQueries.getGoldenIds());
   }

   public Either<MpiGeneralError, List<String>> fetchGoldenIds(
         final long offset,
         final long length) {
      return Either.right(DgraphQueries.fetchGoldenIds(offset, length));
   }

   public Either<MpiGeneralError, List<GoldenRecord>> findLinkCandidates(final DemographicData demographicData) {
      return Either.right(DgraphQueries.findLinkCandidates(demographicData));
   }

   public Either<MpiGeneralError, List<GoldenRecord>> findMatchCandidates(final DemographicData demographicData) {
      return Either.right(DgraphQueries.findMatchCandidates(demographicData));
   }

   public Either<MpiGeneralError, PaginatedResultSet<GoldenRecord>> apiCrFindGoldenRecords(final ApiModels.ApiCrFindRequest request) {
      final var goldenRecords = DgraphQueries.findGoldenRecords(request);
      if (goldenRecords.isRight()) {
         return Either.right(goldenRecords.get()); // .all().stream().map(CustomDgraphGoldenRecord::toGoldenRecord).toList());
      } else {
         return Either.left(goldenRecords.getLeft());
      }
   }

   private PaginatedResultSet<ExpandedGoldenRecord> paginatedExpandedGoldenRecords(final PaginatedResultSet<ExpandedGoldenRecord> list) {
      return list;
   }

   private PaginatedResultSet<Interaction> paginatedInteractions(final PaginatedResultSet<InteractionWithScore> list) {
      return new PaginatedResultSet<>(list.data().stream().map(InteractionWithScore::interaction).toList(), list.pagination());
   }

   private LibMPIPaginatedResultSet<String> paginatedGids(final DgraphPaginatedUidList list) {
      if (list == null) {
         return null;
      }
      final var data = list.all().stream().map(DgraphUid::uid).toList();
      final var pagination = list.pagination().getFirst();
      return new LibMPIPaginatedResultSet<>(data, pagination);
   }

   private PaginatedGIDsWithInteractionCount paginatedGidsWithInteractionCount(final DgraphPaginationUidListWithInteractionCount list) {
      if (list == null) {
         return null;
      }
      final var data = list.all().stream().map(DgraphUid::uid).toList();
      final var pagination = list.pagination().getFirst();
      final var interactionCount = list.interactionCount().getFirst();
      return new PaginatedGIDsWithInteractionCount(data, pagination, interactionCount);
   }

   public Either<MpiGeneralError, PaginatedResultSet<ExpandedGoldenRecord>> simpleSearchGoldenRecords(
         final List<ApiModels.ApiSearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      final var list = DgraphQueries.simpleSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
      return Either.right(paginatedExpandedGoldenRecords(list));
   }

   public Either<MpiGeneralError, PaginatedResultSet<ExpandedGoldenRecord>> customSearchGoldenRecords(
         final List<ApiModels.ApiSimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      final var list = DgraphQueries.customSearchGoldenRecords(params, offset, limit, sortBy, sortAsc);
      return Either.right(paginatedExpandedGoldenRecords(list));
   }

   public Either<MpiGeneralError, PaginatedResultSet<Interaction>> simpleSearchInteractions(
         final List<ApiModels.ApiSearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      final var list = DgraphQueries.simpleSearchInteractions(params, offset, limit, sortBy, sortAsc);
      return Either.right(paginatedInteractions(list));
   }

   public Either<MpiGeneralError, PaginatedResultSet<Interaction>> customSearchInteractions(
         final List<ApiModels.ApiSimpleSearchRequestPayload> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      final var list = DgraphQueries.customSearchInteractions(params, offset, limit, sortBy, sortAsc);
      return Either.right(paginatedInteractions(list));
   }

   public Either<MpiGeneralError, LibMPIPaginatedResultSet<String>> filterGids(
         final List<ApiModels.ApiSearchParameter> params,
         final LocalDateTime createdAt,
         final PaginationOptions paginationOptions) {
      final var list = DgraphQueries.filterGidsWithParams(params, createdAt, paginationOptions, false);
      return Either.right(paginatedGids(list.getLeft()));
   }

   public Either<MpiGeneralError, PaginatedGIDsWithInteractionCount> filterGidsWithInteractionCount(
         final List<ApiModels.ApiSearchParameter> params,
         final LocalDateTime createdAt,
         final PaginationOptions paginationOptions) {
      final var list = DgraphQueries.filterGidsWithParams(params, createdAt, paginationOptions, true);
      return Either.right(paginatedGidsWithInteractionCount(list.get()));
   }


   /*
    * *******************************************************
    * MUTATIONS
    * *******************************************************
    */

   public Either<MpiGeneralError, ApiModels.ApiCivilRecordResponse> insertCivilRecord(
         final String auxId,
         final DemographicData demographicData) {
      return Either.left(new MpiServiceError.NotImplementedError("insertCivilRecord not implemented for DGraph"));
   }

   public Option<MpiGeneralError> setScore(
         final String interactionUID,
         final String goldenRecordUid,
         final Float score) {
      return dgraphMutations.setScore(interactionUID, goldenRecordUid, score)
            ? Option.none()
            : Option.of(new MpiServiceError.InternalError("setScore error"));
   }

   public Option<MpiGeneralError> updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final String val) {
      return dgraphMutations.updateGoldenRecordField(goldenId, fieldName, val)
            ? Option.none()
            : Option.of(new MpiServiceError.InternalError("updateGoldenRecordField error"));
   }

   public Option<MpiGeneralError> updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final Boolean val) {
      return dgraphMutations.updateGoldenRecordField(goldenId, fieldName, val)
            ? Option.none()
            : Option.of(new MpiServiceError.InternalError("updateGoldenRecordField error"));
   }

   public Option<MpiGeneralError> updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final Double val) {
      return dgraphMutations.updateGoldenRecordField(goldenId, fieldName, val)
            ? Option.none()
            : Option.of(new MpiServiceError.InternalError("updateGoldenRecordField error"));
   }

   public Option<MpiGeneralError> updateGoldenRecordField(
         final String goldenId,
         final String fieldName,
         final Long val) {
      return dgraphMutations.updateGoldenRecordField(goldenId, fieldName, val)
            ? Option.none()
            : Option.of(new MpiServiceError.InternalError("updateGoldenRecordField error"));
   }

   public Either<MpiGeneralError, LinkInfo> linkToNewGoldenRecord(
         final String goldenUID,
         final String interactionUID,
         final Float score) {
      return dgraphMutations.linkToNewGoldenRecord(goldenUID, interactionUID, score);
   }

   public Either<MpiGeneralError, LinkInfo> updateLink(
         final String goldenUID,
         final String newGoldenUID,
         final String interactionUID,
         final Float score) {
      return dgraphMutations.updateLink(goldenUID, newGoldenUID, interactionUID, score);
   }

   public Either<MpiGeneralError, LinkInfo> createInteractionAndLinkToExistingGoldenRecord(
         final Interaction interaction,
         final GoldenIdScore goldenIdScore) {
      return Either.right(dgraphMutations.linkDGraphInteraction(interaction, goldenIdScore));
   }

   public Either<MpiGeneralError, LinkInfo> createInteractionAndLinkToClonedGoldenRecord(
         final Interaction interaction,
         final Float score) {
      return Either.right(dgraphMutations.addNewDGraphInteraction(interaction));
   }

   /*
    * *******************************************************
    * DATABASE
    * *******************************************************
    */

   public Option<MpiGeneralError> connect() {
      DgraphClient.getInstance().connect();
      return Option.none();
   }

   public Option<MpiGeneralError> dropAll() {
      connect();
      try {
         DgraphClient.getInstance().alter(DgraphProto.Operation.newBuilder().setDropAll(true).build());
         return Option.none();
      } catch (RuntimeException e) {
         LOGGER.error(e.getMessage(), e);
         return Option.of(new MpiServiceError.GeneralError("Drop All Error"));
      }
   }

   public Option<MpiGeneralError> dropAllData() {
      connect();
      try {
         DgraphClient.getInstance().alter(DgraphProto.Operation.newBuilder().setDropOp(DATA).build());
         return Option.none();
      } catch (RuntimeException e) {
         LOGGER.error(e.getMessage());
         return Option.of(new MpiServiceError.GeneralError("Drop All Data Error"));
      }
   }

   public Option<MpiGeneralError> createSchema() {
      connect();
      return dgraphMutations.createSchema();
   }

}
