package org.procj.provider.jdbc;

import java.sql.Connection;
import org.procj.provider.spi.Procedure;
import org.procj.provider.spi.ProcedureExecutor;

public class JdbcProcedureExecutor implements ProcedureExecutor {

  private final Connection connection;

  public JdbcProcedureExecutor(Connection connection) {
    this.connection = connection;
  }

  @Override
  public Procedure getProcedure(String signature) throws Exception {
    return new JdbcProcedure(signature, connection);
  }

  @Override
  public void shutdown() throws Exception {
    connection.close();
  }

  @Override
  public void commit() throws Exception {
    connection.commit();
  }

  @Override
  public void rollback() throws Exception {
    connection.rollback();
  }
}
