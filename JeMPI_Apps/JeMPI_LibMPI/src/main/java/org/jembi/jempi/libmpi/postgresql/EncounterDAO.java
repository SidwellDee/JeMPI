package org.jembi.jempi.libmpi.postgresql;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.shared.config.Config;
import org.jembi.jempi.shared.models.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class EncounterDAO extends GenericDAO<EncounterDAO.SqlEncounter> {

   private static final Logger LOGGER = LogManager.getLogger(EncounterDAO.class);

   @Override
   UUID insert(
         final PsqlClient client,
         final SqlEncounter entity) throws SQLException {
      UUID uuid = null;
      final var sql = String.format(
            Locale.ROOT,
            """
            INSERT INTO %s (pin,
                            first_name,
                            middle_name,
                            surname,
                            sex,
                            dob,
                            birth_time,
                            cell_phone,
                            inkhundla,
                            chiefdom,
                            nationality,
                            city,
                            golden_record_uid,
                            score,
                            source_id_uid,
                            aux_date_created,
                            aux_id)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);
            """.stripIndent(), getTableName());
      try (var pstmt = client.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
         for (int i = 0; i < Config.FIELDS_CONFIG.demographicFields.size(); i++) {
            final var value = entity.getDemographicField(i);
            if (StringUtils.isBlank(value)) {
               pstmt.setString(i + 1, StringUtils.EMPTY);
            } else {
               pstmt.setString(i + 1, entity.getDemographicField(i));
            }
         }
         pstmt.setObject(13, entity.goldenRecordUid);
         pstmt.setFloat(14, entity.score());
         pstmt.setObject(15, entity.sourceIdUid());
         pstmt.setTimestamp(16, Timestamp.valueOf(entity.auxDateCreated));
         pstmt.setString(17, entity.auxId());
         final var affectedRows = pstmt.executeUpdate();
         if (affectedRows > 0) {
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
               if (rs != null && rs.next()) {
                  uuid = UUID.fromString(rs.getString("uid"));
               } else {
                  LOGGER.error("No generated keys");
               }
            }
         }
      }
      return uuid;
   }

   int countEncountersForGoldenId(
         final PsqlClient client,
         final UUID goldenRecordUid) throws SQLException {
      final var sql = "select count(*) from encounters where golden_record_uid = ?;";
      try (PreparedStatement pstmt = client.prepareStatement(sql)) {
         pstmt.setObject(1, goldenRecordUid);
         ResultSet rs = pstmt.executeQuery();
         if (rs.next()) {
            return rs.getInt("count");
         }
      }
      return -1;
   }

   @Override
   SqlEncounter getById(
         final PsqlClient client,
         final UUID id) throws SQLException {
      final var sql = "select * from encounters where uid = ?;";
      SqlEncounter entity = null;
      try (PreparedStatement pstmt = client.prepareStatement(sql)) {
         pstmt.setObject(1, id);
         ResultSet rs = pstmt.executeQuery();
         if (rs.next()) {
            entity = new SqlEncounter(
                  rs.getObject("uid", UUID.class),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(0).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(1).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(2).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(3).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(4).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(5).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(6).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(7).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(8).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(9).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(10).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(11).scName()),
                  id,
                  rs.getFloat("score"),
                  rs.getObject("source_id_uid", UUID.class),
                  rs.getTimestamp(Config.FIELDS_CONFIG.auxInteractionFields.get(0).scName()).toLocalDateTime(),
                  rs.getString(Config.FIELDS_CONFIG.userAuxInteractionFields.get(0).scName()));
         }
      }
      return entity;
   }

   List<SqlEncounter> getEncountersForGoldenId(
         final PsqlClient client,
         final UUID id) throws SQLException {
      final var sql = "select * from encounters where golden_record_uid = ?;";

      List<SqlEncounter> entities = new LinkedList<>();
      try (PreparedStatement pstmt = client.prepareStatement(sql)) {
         pstmt.setObject(1, id);
         ResultSet rs = pstmt.executeQuery();
         while (rs.next()) {
            final var entity = new EncounterDAO.SqlEncounter(
                  rs.getObject("uid", UUID.class),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(0).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(1).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(2).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(3).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(4).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(5).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(6).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(7).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(8).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(9).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(10).scName()),
                  rs.getString(Config.FIELDS_CONFIG.demographicFields.get(11).scName()),
                  id,
                  rs.getFloat("score"),
                  rs.getObject("source_id_uid", UUID.class),
                  rs.getTimestamp(Config.FIELDS_CONFIG.auxInteractionFields.get(0).scName()).toLocalDateTime(),
                  rs.getString(Config.FIELDS_CONFIG.userAuxInteractionFields.get(0).scName()));
            entities.add(entity);
         }
      }
      return entities;
   }

   Interaction mapToInteraction(
         final SqlEncounter sqlEncounter,
         final SourceIdDAO.SqlSourceId sqlSourceId) {
      final var demographicData = new DemographicData();
      for (int i = 0; i < Config.FIELDS_CONFIG.demographicFields.size(); i++) {
         demographicData.fields.add(
               new DemographicData.DemographicField(
                     Config.FIELDS_CONFIG.demographicFields.get(i).ccName(),
                     sqlEncounter.getDemographicField(i)));
      }
      return new Interaction(sqlEncounter.uid().toString(),
                             new SourceId(sqlSourceId.uid().toString(), sqlSourceId.facilityCode(), sqlSourceId.patientId()),
                             sqlEncounter.getAuxInteractionData(),
                             demographicData);
   }

   boolean updateScore(
         final PsqlClient client,
         final UUID encounterUid,
         final UUID goldenUid,
         final float score) throws SQLException {
      final String sql = "UPDATE encounters SET score = ? WHERE uid = ?;";
      try (PreparedStatement pstmt = client.prepareStatement(sql)) {
         pstmt.setFloat(1, score);
         pstmt.setObject(2, encounterUid);
         final var rs = pstmt.executeUpdate();
         return rs == 1;
      }
   }

   @Override
   protected String getTableName() {
      return "encounters";
   }

   public record SqlEncounter(
         UUID uid,
         String pin,
         String firstName,
         String middleName,
         String surname,
         String sex,
         String dob,
         String birthTime,
         String cellPhone,
         String inkhundla,
         String chiefdom,
         String nationality,
         String city,
         UUID goldenRecordUid,
         float score,
         UUID sourceIdUid,
         LocalDateTime auxDateCreated,
         String auxId) {

      AuxInteractionData getAuxInteractionData() {
         final var userAuxList = new LinkedList<AuxInteractionData.AuxInteractionUserField>();
         userAuxList.add(new AuxInteractionData.AuxInteractionUserField(
               Config.FIELDS_CONFIG.userAuxInteractionFields.getFirst().scName(),
               Config.FIELDS_CONFIG.userAuxInteractionFields.getFirst().ccName(),
               auxId()));
         return new AuxInteractionData(auxDateCreated, userAuxList);
      }

      String getDemographicField(final int index) {
         return switch (index) {
            case 0 -> pin();
            case 1 -> firstName();
            case 2 -> middleName();
            case 3 -> surname();
            case 4 -> sex();
            case 5 -> dob();
            case 6 -> birthTime();
            case 7 -> cellPhone();
            case 8 -> inkhundla();
            case 9 -> chiefdom();
            case 10 -> nationality();
            case 11 -> city();
            default -> throw new IllegalArgumentException();
         };
      }
   }
}
