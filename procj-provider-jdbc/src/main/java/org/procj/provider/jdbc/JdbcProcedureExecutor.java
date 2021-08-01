package org.procj.provider.jdbc;

import java.sql.Connection;
import lombok.AllArgsConstructor;
import org.procj.provider.spi.Procedure;
import org.procj.provider.spi.ProcedureExecutor;

@AllArgsConstructor
public class JdbcProcedureExecutor implements ProcedureExecutor {

  private final Connection connection;

  @Override
  public Procedure getProcedure(String signature) throws Exception {
    return new JdbcProcedure(connection, signature);
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
