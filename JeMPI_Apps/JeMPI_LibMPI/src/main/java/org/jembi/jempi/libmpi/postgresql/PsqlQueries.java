package org.jembi.jempi.libmpi.postgresql;

import io.vavr.control.Either;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.MpiException;
import org.jembi.jempi.libmpi.MpiGeneralError;
import org.jembi.jempi.libmpi.MpiServiceError;
import org.jembi.jempi.libmpi.common.PaginatedResultSet;
import org.jembi.jempi.shared.config.Config;
import org.jembi.jempi.shared.models.*;
import org.jembi.jempi.shared.utils.AppUtils;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

final class PsqlQueries {
   private static final Logger LOGGER = LogManager.getLogger(PsqlQueries.class);

   //   private static PsqlClient PSQL_CLIENT;
   private static final GoldenRecordDAO GOLDEN_RECORD_DAO = new GoldenRecordDAO();
   private static final EncounterDAO ENCOUNTER_DAO = new EncounterDAO();
   private static final SourceIdDAO SOURCE_ID_DAO = new SourceIdDAO();

   private PsqlQueries() {
   }

   static void connect(final PsqlClient psqlClient) throws SQLException, MpiException {
      psqlClient.connect();
   }

   private static Either<MpiGeneralError, GoldenRecord> getGoldenRecord(
         final PsqlClient psqlClient,
         final String uid) {
      GoldenRecord goldenRecord;
      try {
         psqlClient.connect();
         final var sqlGoldenRecord = GOLDEN_RECORD_DAO.getById(psqlClient, UUID.fromString(uid));
         final var demographicData = new DemographicData();
         for (int i = 0; i < Config.FIELDS_CONFIG.demographicFields.size(); i++) {
            demographicData.fields.add(
                  new DemographicData.DemographicField(
                        Config.FIELDS_CONFIG.demographicFields.get(i).ccName(),
                        sqlGoldenRecord.getDemographicField(i)));
         }
         List<AuxGoldenRecordData.AuxGoldenRecordUserField> auxUserFields = new LinkedList<>();
         auxUserFields.add(new AuxGoldenRecordData.AuxGoldenRecordUserField(
               Config.FIELDS_CONFIG.userAuxGoldenRecordFields.getFirst().ccName(), sqlGoldenRecord.auxId()));
         final var auxGoldenRecordData = new AuxGoldenRecordData(
               sqlGoldenRecord.auxDateCreated(),
               sqlGoldenRecord.auxAutoUpdate(),
               auxUserFields);

         final var sidList = SOURCE_ID_DAO.getSourceIdsForGoldenId(psqlClient, UUID.fromString(uid));
         goldenRecord = new GoldenRecord(
               uid,
               sidList.stream().map(sid -> new SourceId(sid.uid().toString(), sid.facilityCode(), sid.patientId())).toList(),
               auxGoldenRecordData,
               demographicData);
      } catch (SQLException | MpiException e) {
         LOGGER.error(e.getMessage(), e);
         return Either.left(new MpiServiceError.InternalError(e.getMessage()));
      }
      return Either.right(goldenRecord);
   }

   static Either<MpiGeneralError, PaginatedResultSet<GoldenRecord>> getGoldenRecord(
         final PsqlClient psqlClient,
         final ApiModels.ApiCrFindRequest req) {

      List<GoldenRecord> goldenRecords;
      try {
         psqlClient.connect();
         final var setFunctions = new HashSet<String>();
         setFunctions.add("eq");
         setFunctions.add("match");
         final var setOperators = new HashSet<String>();
         setOperators.add("and");
         setOperators.add("or");

         final var operand = req.operand();
         StringBuilder queryBuilder;
         if (operand.name().equals("pin")) {
            queryBuilder =
                  new StringBuilder("select * from golden_records "
                                    + "inner join source_id on golden_records.uid = source_id.golden_record_uid "
                                    + "where '").append(operand.value())
                                             .append("' in(pin,patient_id)");
         } else {
            queryBuilder =
                  new StringBuilder("select * from golden_records where lower(").append(AppUtils.camelToSnake(operand.name()))
                                                                          .append(") = lower('").append(operand.value()).append("')");
         }

         if (req.operands() != null) {
            for (ApiModels.ApiCrFindRequest.ApiLogicalOperand op2 : req.operands()) {
               if (!op2.operand().name().equals(operand.name())) {
                  queryBuilder
                        .append(" ")
                        .append(op2.operator())
                        .append(" lower(")
                        .append(AppUtils.camelToSnake(op2.operand().name()))
                        .append(") = lower('")
                        .append(op2.operand().value())
                        .append("')");
               }
            }
         }

         final var query = queryBuilder.toString();

         LOGGER.info("PSQL QUERY: {}", query);

         goldenRecords = findGoldenRecord(psqlClient, query);
      } catch (SQLException | MpiException e) {
         LOGGER.error(e.getMessage(), e);
         return Either.left(new MpiServiceError.InternalError(e.getMessage()));
      }

      return Either.right(new PaginatedResultSet<>(goldenRecords, List.of(new LibMPIPagination(goldenRecords.size()))));
   }

   private static List<GoldenRecord> findGoldenRecord(
         final PsqlClient psqlClient,
         final String query) {
      List<GoldenRecord> goldenRecords = new LinkedList<>();

      try{
         final var sqlGoldenRecords = GOLDEN_RECORD_DAO.findExactDemographics(psqlClient, query);

         for (GoldenRecordDAO.SqlGoldenRecord candidate : sqlGoldenRecords) {
            final var demographicFields = new DemographicData();
            for (int i = 0; i < Config.FIELDS_CONFIG.demographicFields.size(); i++) {
               demographicFields.fields.add(
                     new DemographicData.DemographicField(
                           Config.FIELDS_CONFIG.demographicFields.get(i).ccName(),
                           candidate.getDemographicField(i)));
            }
            final List<AuxGoldenRecordData.AuxGoldenRecordUserField> auxGoldenRecordUserFields = new LinkedList<>();
            auxGoldenRecordUserFields.add(new AuxGoldenRecordData.AuxGoldenRecordUserField("aux_id", candidate.auxId()));
            final var auxGoldenRecordData = new AuxGoldenRecordData(
                  candidate.auxDateCreated(),
                  candidate.auxAutoUpdate(),
                  auxGoldenRecordUserFields
            );
            final var goldenRecord = new GoldenRecord(candidate.uid().toString(),
                                                      null,
                                                      auxGoldenRecordData,
                                                      demographicFields);
            goldenRecords.add(goldenRecord);
         }
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }

      return goldenRecords;
   }

   private static List<InteractionWithScore> getInteractionsWithScore(
         final PsqlClient psqlClient,
         final String uid) {
      try {
         psqlClient.connect();
         final var sqlEncounters = ENCOUNTER_DAO.getEncountersForGoldenId(psqlClient, UUID.fromString(uid));
         return sqlEncounters.stream()
                             .map(sqlEncounter -> {
                                final var demographicData = new DemographicData();
                                for (int i = 0; i < Config.FIELDS_CONFIG.demographicFields.size(); i++) {
                                   demographicData.fields.add(
                                         new DemographicData.DemographicField(
                                               Config.FIELDS_CONFIG.demographicFields.get(i).ccName(),
                                               sqlEncounter.getDemographicField(i)));
                                }
                                SourceId sourceId = null;
                                try {
                                   final var sid = SOURCE_ID_DAO.getById(psqlClient, sqlEncounter.sourceIdUid());
                                   sourceId = new SourceId(sid.uid().toString(), sid.facilityCode(), sid.patientId());
                                } catch (SQLException e) {
                                   LOGGER.error(e.getLocalizedMessage(), e);
                                }
                                return new InteractionWithScore(new Interaction(sqlEncounter.uid().toString(),
                                                                                sourceId,
                                                                                sqlEncounter.getAuxInteractionData(),
                                                                                demographicData),
                                                                sqlEncounter.score());
                             })
                             .toList();
      } catch (SQLException | MpiException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
      return List.of();
   }

   static Either<MpiGeneralError, Long> countInteractions(final PsqlClient psqlClient) {
      try {
         psqlClient.connect();
         return ENCOUNTER_DAO.count(psqlClient);
      } catch (SQLException | MpiException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Either.left(new MpiServiceError.InternalError(e.getLocalizedMessage()));
      }
   }

   static Either<MpiGeneralError, Long> countGoldenRecords(final PsqlClient psqlClient) {
      try {
         psqlClient.connect();
         return GOLDEN_RECORD_DAO.count(psqlClient);
      } catch (SQLException | MpiException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Either.left(new MpiServiceError.InternalError(e.getLocalizedMessage()));
      }
   }

   static Either<MpiGeneralError, List<String>> findGoldenIds(final PsqlClient psqlClient) {
      try {
         psqlClient.connect();
         final var uuidList = GOLDEN_RECORD_DAO.getUid(psqlClient);
         return Either.right(uuidList.stream().map(UUID::toString).toList());
      } catch (SQLException | MpiException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Either.left(new MpiServiceError.InternalError(e.getLocalizedMessage()));
      }
   }

   static Either<MpiGeneralError, PaginatedResultSet<ExpandedGoldenRecord>> findExpandedGoldenRecords(
         final PsqlClient psqlClient,
         final List<String> goldenIds) {
      final List<ExpandedGoldenRecord> list = new LinkedList<>();
      for (String goldenId : goldenIds) {
         final var goldenRecord = getGoldenRecord(psqlClient, goldenId);
         if (goldenRecord.isLeft()) {
            return Either.left(goldenRecord.getLeft());
         }
         final var interactionsWithScore = getInteractionsWithScore(psqlClient, goldenId);
         final var expandedGoldenRecord = new ExpandedGoldenRecord(goldenRecord.get(), interactionsWithScore);
         list.add(expandedGoldenRecord);
      }
      final var nGoldenRecords = countGoldenRecords(psqlClient);
      if (nGoldenRecords.isLeft()) {
         return Either.left(nGoldenRecords.getLeft());
      }
      return Either.right(new PaginatedResultSet<>(list, List.of(new LibMPIPagination(nGoldenRecords.get().intValue()))));
   }

   static Either<MpiGeneralError, List<ExpandedInteraction>> findExpandedInteractions(
         final PsqlClient psqlClient,
         final List<String> encounterIdList) {
      final List<ExpandedInteraction> list = new LinkedList<>();
      for (String encounterId : encounterIdList) {
         try {
            final var sqlEncounter = ENCOUNTER_DAO.getById(psqlClient, UUID.fromString(encounterId));
            final var sqlSourceId = SOURCE_ID_DAO.getById(psqlClient, sqlEncounter.sourceIdUid());
            final var encounter = ENCOUNTER_DAO.mapToInteraction(sqlEncounter, sqlSourceId);
            final var goldenRecord = getGoldenRecord(psqlClient, sqlEncounter.goldenRecordUid().toString());
            if (goldenRecord.isLeft()) {
               return Either.left(goldenRecord.getLeft());
            }
            final var expandedInteraction = new ExpandedInteraction(
                  encounter,
                  List.of(new GoldenRecordWithScore(goldenRecord.get(), sqlEncounter.score())));
            list.add(expandedInteraction);
         } catch (SQLException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            return Either.left(new MpiServiceError.InternalError(e.getLocalizedMessage()));
         }
      }
      return Either.right(list);
   }

   static Either<MpiGeneralError, List<GoldenRecord>> findLinkCandidates(
         final PsqlClient psqlClient,
         final DemographicData demographicData) {
      final var list = new LinkedList<GoldenRecord>();
      final List<GoldenRecordDAO.SqlGoldenRecord> candidates;
      try {
         psqlClient.connect();
         candidates = GOLDEN_RECORD_DAO.findLinkCandidates(psqlClient, demographicData);
         for (GoldenRecordDAO.SqlGoldenRecord candidate : candidates) {
            final var demographicFields = new DemographicData();
            for (int i = 0; i < Config.FIELDS_CONFIG.demographicFields.size(); i++) {
               demographicFields.fields.add(
                     new DemographicData.DemographicField(
                           Config.FIELDS_CONFIG.demographicFields.get(i).ccName(),
                           candidate.getDemographicField(i)));
            }
            final List<AuxGoldenRecordData.AuxGoldenRecordUserField> auxGoldenRecordUserFields = new LinkedList<>();
            auxGoldenRecordUserFields.add(new AuxGoldenRecordData.AuxGoldenRecordUserField("aux_id", candidate.auxId()));
            final var auxGoldenRecordData = new AuxGoldenRecordData(
                  candidate.auxDateCreated(),
                  candidate.auxAutoUpdate(),
                  auxGoldenRecordUserFields
            );
            final var goldenRecord = new GoldenRecord(candidate.uid().toString(),
                                                      null,
                                                      auxGoldenRecordData,
                                                      demographicFields);
            list.add(goldenRecord);
         }
      } catch (SQLException | MpiException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Either.left(new MpiServiceError.InternalError(e.getLocalizedMessage()));
      }
      return Either.right(list);
   }

   static Either<MpiGeneralError, PaginatedResultSet<ExpandedGoldenRecord>> simpleSearchGoldenRecords(
         final PsqlClient psqlClient,
         final List<ApiModels.ApiSearchParameter> params,
         final Integer offset,
         final Integer limit,
         final String sortBy,
         final Boolean sortAsc) {
      try {
         final var uidList = GOLDEN_RECORD_DAO.getPaginatedUID(psqlClient, offset, limit, sortBy, sortAsc);
         return findExpandedGoldenRecords(psqlClient, uidList.stream().map(UUID::toString).toList());
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         return Either.left(new MpiServiceError.InternalError(e.getLocalizedMessage()));
      }
   }

}
