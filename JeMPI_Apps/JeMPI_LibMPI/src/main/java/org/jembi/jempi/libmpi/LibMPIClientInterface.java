package org.jembi.jempi.libmpi;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.jembi.jempi.libmpi.common.PaginatedResultSet;
import org.jembi.jempi.shared.models.*;

import java.time.LocalDateTime;
import java.util.List;

public interface LibMPIClientInterface {

   /*
    * *****************************************************************************
    * *
    * Database
    * *****************************************************************************
    * *
    */
   Option<MpiGeneralError> connect();

   Option<MpiGeneralError> dropAll();

   Option<MpiGeneralError> dropAllData();

   Option<MpiGeneralError> createSchema();

   /*
    * *****************************************************************************
    * *
    * Queries
    * *****************************************************************************
    * *
    */

   Either<MpiGeneralError, Long> countInteractions();

   Either<MpiGeneralError, Long> countGoldenRecords();

   Either<MpiGeneralError, List<SourceId>> findSourceId(
         String facility,
         String client);

   Either<MpiGeneralError, List<ExpandedSourceId>> findExpandedSourceIdList(
         String facility,
         String client);

   Either<MpiGeneralError, Interaction> findInteraction(String interactionID);

   Either<MpiGeneralError, List<Interaction>> findInteractions(List<String> interactionIDs);

   Either<MpiGeneralError, List<ExpandedInteraction>> findExpandedInteractions(List<String> interactionIDs);

   Either<MpiGeneralError, PaginatedResultSet<GoldenRecord>> findGoldenRecords(List<String> goldenIds);

   Either<MpiGeneralError, PaginatedResultSet<ExpandedGoldenRecord>> findExpandedGoldenRecords(List<String> goldenIds);

   Either<MpiGeneralError, List<String>> findGoldenIds();

   Either<MpiGeneralError, List<String>> fetchGoldenIds(
         long offset,
         long length);

   Either<MpiGeneralError, List<GoldenRecord>> findLinkCandidates(DemographicData demographicData);

   Either<MpiGeneralError, String> restoreGoldenRecord(RestoreGoldenRecords goldenRecord);

   Either<MpiGeneralError, List<GoldenRecord>> findMatchCandidates(DemographicData demographicData);

   Either<MpiGeneralError, PaginatedResultSet<ExpandedGoldenRecord>> simpleSearchGoldenRecords(
         List<ApiModels.ApiSearchParameter> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc);

   Either<MpiGeneralError, PaginatedResultSet<ExpandedGoldenRecord>> customSearchGoldenRecords(
         List<ApiModels.ApiSimpleSearchRequestPayload> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc);

   Either<MpiGeneralError, PaginatedResultSet<Interaction>> simpleSearchInteractions(
         List<ApiModels.ApiSearchParameter> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc);

   Either<MpiGeneralError, PaginatedResultSet<Interaction>> customSearchInteractions(
         List<ApiModels.ApiSimpleSearchRequestPayload> params,
         Integer offset,
         Integer limit,
         String sortBy,
         Boolean sortAsc);

   Either<MpiGeneralError, LibMPIPaginatedResultSet<String>> filterGids(
         List<ApiModels.ApiSearchParameter> params,
         LocalDateTime createdAt,
         PaginationOptions paginationOptions);

   Either<MpiGeneralError, PaginatedGIDsWithInteractionCount> filterGidsWithInteractionCount(
         List<ApiModels.ApiSearchParameter> params,
         LocalDateTime createdAt,
         PaginationOptions paginationOptions);

   Either<MpiGeneralError, PaginatedResultSet<GoldenRecord>> apiCrFindGoldenRecords(ApiModels.ApiCrFindRequest request);

   /*
    * *****************************************************************************
    * *
    * Mutations
    * *****************************************************************************
    * *
    */

   Either<MpiGeneralError, ApiModels.ApiCivilRecordResponse> insertCivilRecord(
         String auxId,
         DemographicData demographicData);

   Option<MpiGeneralError> setScore(
         String interactionUID,
         String goldenRecordUid,
         Float score);

   Option<MpiGeneralError> updateGoldenRecordField(
         String goldenId,
         String fieldName,
         String value);

   Option<MpiGeneralError> updateGoldenRecordField(
         String goldenId,
         String fieldName,
         Boolean value);

   Option<MpiGeneralError> updateGoldenRecordField(
         String goldenId,
         String fieldName,
         Double value);

   Option<MpiGeneralError> updateGoldenRecordField(
         String goldenId,
         String fieldName,
         Long value);

   Either<MpiGeneralError, LinkInfo> linkToNewGoldenRecord(
         String currentGoldenId,
         String interactionId,
         Float score);

   Either<MpiGeneralError, LinkInfo> updateLink(
         String goldenId,
         String newGoldenId,
         String interactionId,
         Float score);

   Either<MpiGeneralError, LinkInfo> createInteractionAndLinkToExistingGoldenRecord(
         Interaction interaction,
         GoldenIdScore goldenIdScore);

   Either<MpiGeneralError, LinkInfo> createInteractionAndLinkToClonedGoldenRecord(
         Interaction interaction,
         Float score);

   record GoldenIdScore(
         String goldenId,
         Float score) {
   }

}
