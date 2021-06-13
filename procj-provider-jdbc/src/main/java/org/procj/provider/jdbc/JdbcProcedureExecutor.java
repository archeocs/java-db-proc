package org.procj.provider.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import org.procj.provider.spi.Procedure;
import org.procj.provider.spi.ProcedureExecutor;

public class JdbcProcedureExecutor implements ProcedureExecutor {

  private final Connection connection;

  public JdbcProcedureExecutor(Connection connection) {
    this.connection = connection;
  }

  @Override
  public Procedure getProcedure(String signature) {
    return new JdbcProcedure(signature, connection);
  }

  @Override
  public void shutdown() {
    try {
      connection.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void commit() {
    try {
      connection.commit();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void rollback() {
    try {
      connection.rollback();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
