package org.procj.provider.hibernate;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.procj.provider.spi.Procedure;
import org.procj.provider.spi.ProcedureExecutor;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public class HibernateProcedureExecutorProviderIT {

  public static final DockerImageName MYSQL_IMAGE = DockerImageName.parse("mysql:5.7.22");
  private static MySQLContainer<?> MYSQL;

  @SuppressWarnings({"resource", "rawtypes"})
  @BeforeAll
  public static void setupConainer() {
    MYSQL =
        (MySQLContainer<?>) new MySQLContainer(MYSQL_IMAGE).withInitScript("init-procedures.sql");
    MYSQL.start();
  }

  @AfterAll
  public static void shutodown() {
    MYSQL.stop();
  }

  @Test
  public void shouldReadSingleValue() throws Exception {
    final Properties props = new Properties();
    props.setProperty("hibernate.connection.driver_class", MYSQL.getDriverClassName());
    props.setProperty("hibernate.connection.url", MYSQL.getJdbcUrl());
    props.setProperty("hibernate.connection.username", MYSQL.getUsername());
    props.setProperty("hibernate.connection.password", MYSQL.getPassword());
    props.setProperty("hibernate.connection.useSSL", "false");
    props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL57Dialect");
    final HibernateProcedureExecutorProvider provider = new HibernateProcedureExecutorProvider();
    final ProcedureExecutor executor = provider.initExecutor(props);
    final Procedure procedure = executor.getProcedure("no_args");
    procedure.execute();

    final List<List<?>> rows = getAll("SELECT * FROM INPUT");
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0)).contains("no-args");
    assertThat(procedure.getReturnValue()).isNull();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldReadFromCursor() throws Exception {
    final Properties props = new Properties();
    props.setProperty("hibernate.connection.driver_class", MYSQL.getDriverClassName());
    props.setProperty("hibernate.connection.url", MYSQL.getJdbcUrl());
    props.setProperty("hibernate.connection.username", MYSQL.getUsername());
    props.setProperty("hibernate.connection.password", MYSQL.getPassword());
    props.setProperty("hibernate.connection.useSSL", "false");
    props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL57Dialect");
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

  List<List<?>> getAll(String sql) throws Exception {
    Class.forName(MYSQL.getDriverClassName());
    final Properties props = new Properties();
    props.setProperty("useSSL", "false");
    props.setProperty("user", MYSQL.getUsername());
    props.setProperty("password", MYSQL.getPassword());
    final String url = MYSQL.getJdbcUrl();
    final Connection conn = DriverManager.getConnection(MYSQL.getJdbcUrl(), props);
    conn.createStatement().execute("call get_src()");
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
