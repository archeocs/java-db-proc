package org.procj.provider.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import org.procj.provider.spi.ExecutorConfig;
import org.procj.provider.spi.ProcedureExecutor;
import org.procj.provider.spi.ProcedureExecutorProvider;

public class JdbcProcedureExecutorProvider implements ProcedureExecutorProvider {

  @Override
  public ProcedureExecutor initExecutor(Properties properties, ExecutorConfig config) {
    try {
      Class.forName(properties.getProperty("jdbc.driver"));
      Connection con = DriverManager.getConnection(properties.getProperty("url"), properties);
      con.setAutoCommit(config.isAutoCommit());
      return new JdbcProcedureExecutor(con);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getName() {
    return "jdbc";
  }
}
