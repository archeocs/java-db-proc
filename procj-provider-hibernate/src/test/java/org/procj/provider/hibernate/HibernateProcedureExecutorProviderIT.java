package org.procj.provider.hibernate;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.Test;
import org.procj.provider.spi.Procedure;
import org.procj.provider.spi.ProcedureExecutor;
import org.testcontainers.jdbc.ContainerDatabaseDriver;

public class HibernateProcedureExecutorProviderIT {

  @Test
  public void shouldReadSingleValue() throws Exception {
    final Properties props = new Properties();
    props.setProperty(
        "hibernate.connection.driver_class", ContainerDatabaseDriver.class.getCanonicalName());
    props.setProperty(
        "hibernate.connection.url",
        "jdbc:tc:mysql:5.6.23:///databasename?TC_INITSCRIPT=init-procedures.sql");
    props.setProperty("hibernate.connection.username", "mysql");
    props.setProperty("hibernate.connection.password", "mysql");
    final HibernateProcedureExecutorProvider provider = new HibernateProcedureExecutorProvider();
    final ProcedureExecutor executor = provider.initExecutor(props);
    final Procedure procedure = executor.getProcedure("no_args");

    final List<List<?>> rows = getAll("jdbc:tc:mysql:5.6.23:///databasename", "SELECT * FROM src");
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0)).contains("no-args");
    assertThat(procedure.getReturnValue()).isNull();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldReadFromCursor() {
    final Properties props = new Properties();
    props.setProperty(
        "hibernate.connection.driver_class", ContainerDatabaseDriver.class.getCanonicalName());
    props.setProperty(
        "hibernate.connection.url",
        "jdbc:tc:mysql:5.6.23:///databasename?TC_INITSCRIPT=init-procedures.sql");
    props.setProperty("hibernate.connection.username", "mysql");
    props.setProperty("hibernate.connection.password", "mysql");
    final HibernateProcedureExecutorProvider provider = new HibernateProcedureExecutorProvider();
    final ProcedureExecutor executor = provider.initExecutor(props);
    final Procedure proc = executor.getProcedure("get_src");
    proc.execute();
    final List<?> rv = (List<?>) proc.getReturnValue();

    final List<?> expected =
        asList(asList(1, "A"), asList(2, "B"), asList(3, "C"), asList(4, "D"), asList(5, "E"));
    assertThat(rv).hasSize(5);
    for (final Object e : expected) {
      assertThat(expected).contains(e);
    }
  }

  List<List<?>> getAll(String url, String sql) throws Exception {
    Class.forName(ContainerDatabaseDriver.class.getCanonicalName());
    final Connection conn = DriverManager.getConnection(url);
    final ResultSet rs = conn.createStatement().executeQuery(sql);
    final List<List<?>> all = new ArrayList<List<?>>();
    final int columns = rs.getMetaData().getColumnCount();
    while (rs.next()) {
      final ArrayList<Object> row = new ArrayList<Object>();
      for (int i = 1; i <= columns; i++) {
        row.add(rs.getObject(i));
      }
      all.add(row);
    }
    rs.close();
    conn.close();
    return all;
  }
}
