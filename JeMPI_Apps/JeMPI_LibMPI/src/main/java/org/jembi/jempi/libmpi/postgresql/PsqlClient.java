package org.jembi.jempi.libmpi.postgresql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jembi.jempi.libmpi.MpiException;

import java.sql.*;
import java.util.Locale;

final class PsqlClient {

   private static final Logger LOGGER = LogManager.getLogger(PsqlClient.class);
//   private static final int POSTGRESQL_PORT = 5432;
//   private static final String POSTGRESQL_USER = "postgres";
//   private static final String POSTGRESQL_PASSWORD = "instant101";
//   private static final String POSTGRESQL_DB = "mpi_db";
   private final String pgIp;
   private final Integer pgPort;
   private final String pgUser;
   private final String pgPassword;
   private final String pgDb;
   private Connection connection;

   PsqlClient(final String pgIp,
              final Integer pgPort,
              final String pgUser,
              final String pgPassword,
              final String pgDb) {
      this.pgIp = pgIp;
      this.pgPort = pgPort;
      this.pgUser = pgUser;
      this.pgPassword = pgPassword;
      this.pgDb = pgDb;
      connection = null;
   }

   void connect() throws SQLException, MpiException {
      final var url = String.format(Locale.ROOT,
                                    "jdbc:postgresql://%s:%d/%s",
                                    pgIp,
                                    pgPort,
                                    pgDb);
      if (connection == null) {
         try {
            connection = DriverManager.getConnection(url, pgUser, pgPassword);
            connection.setAutoCommit(true);
         } catch (SQLException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            connection = null;
            throw e;
         }
      } else {
         try {
            if (!connection.isValid(5)) {
               connection.close();
               connection = DriverManager.getConnection(url, pgUser, pgPassword);
            }
         } catch (SQLException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            connection = null;
            throw e;
         }
      }

      try {
         if (!connection.isValid(5)) {
            throw new MpiException("Cannot connect to sql server");
         }
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
         connection = null;
         throw e;
      }
   }

   void setAutoCommit(final boolean autoCommit) {
      try {
         connection.setAutoCommit(autoCommit);
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
   }

   void rollback() {
      try {
         connection.rollback();
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
   }

   void commit() {
      try {
         connection.commit();
      } catch (SQLException e) {
         LOGGER.error(e.getLocalizedMessage(), e);
      }
   }

   void disconnect() {
      if (connection != null) {
         try {
            connection.close();
         } catch (SQLException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
         }
         connection = null;
      }
   }

   Statement createStatement() throws SQLException {
      return connection.createStatement();
   }

   PreparedStatement prepareStatement(final String sql) throws SQLException {
      return connection.prepareStatement(sql);
   }

   PreparedStatement prepareStatement(
         final String sql,
         final int resultSetType) throws SQLException {
      return connection.prepareStatement(sql, resultSetType);
   }

}
