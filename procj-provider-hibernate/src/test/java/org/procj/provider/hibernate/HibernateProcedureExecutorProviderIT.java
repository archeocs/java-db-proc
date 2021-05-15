package org.procj.provider.hibernate;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.procj.provider.spi.Procedure;
import org.procj.provider.spi.ProcedureExecutor;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public class HibernateProcedureExecutorProviderIT {

  public static final DockerImageName MYSQL_IMAGE = DockerImageName.parse("mysql:5.7.22");
  private static MySQLContainer<?> MYSQL;
  private ProcedureExecutor testExecutor;

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

  @BeforeEach
  public void initProvider() {
    final HibernateProcedureExecutorProvider prvider = new HibernateProcedureExecutorProvider();
    final Properties hibernateProps = new Properties();
    hibernateProps.setProperty("hibernate.connection.driver_class", MYSQL.getDriverClassName());
    hibernateProps.setProperty("hibernate.connection.url", MYSQL.getJdbcUrl());
    hibernateProps.setProperty("hibernate.connection.username", MYSQL.getUsername());
    hibernateProps.setProperty("hibernate.connection.password", MYSQL.getPassword());
    hibernateProps.setProperty("hibernate.connection.useSSL", "false");
    hibernateProps.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL57Dialect");
    testExecutor = prvider.initExecutor(hibernateProps);
  }

  @AfterEach
  public void shutDown() {
    testExecutor.shutdown();
  }

  @AfterEach
  public void cleanDb() throws Exception {
    deleteAll("INPUT");
  }

  @Test
  public void shouldCallNoArgsProcedure() throws Exception {
    final Procedure procedure = testExecutor.getProcedure("no_args");
    procedure.execute();

    final List<List<Object>> rows = getAll("SELECT * FROM INPUT");
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0)).contains("no-args");
    assertThat(procedure.getReturnValue()).isNull();
  }

  @Test
  public void shouldCallProcedureWithArgs() throws Exception {
    final Procedure procedure = testExecutor.getProcedure("in_args");
    procedure.setParameterIn(1, "test_");
    procedure.setParameterIn(2, "value");
    procedure.execute();

    final List<List<Object>> rows = getAll("SELECT * FROM INPUT");
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0)).contains("test_value");
    assertThat(procedure.getReturnValue()).isNull();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldReadFromCursor() throws Exception {
    final Procedure proc = testExecutor.getProcedure("get_src");
    proc.execute();
    final List<Object> rv = (List<Object>) proc.getReturnValue();
    final List<Object> expected =
        asList(asList(1, "A"), asList(2, "B"), asList(3, "C"), asList(4, "D"), asList(5, "E"));
    assertThat(rv).hasSize(5);
    for (final Object e : expected) {
      assertThat(expected).contains(e);
    }
  }

  private Connection getConnection() throws Exception {
    Class.forName(MYSQL.getDriverClassName());
    final Properties props = new Properties();
    props.setProperty("useSSL", "false");
    props.setProperty("user", MYSQL.getUsername());
    props.setProperty("password", MYSQL.getPassword());
    return DriverManager.getConnection(MYSQL.getJdbcUrl(), props);
  }

  private void deleteAll(String table) throws Exception {
    final Connection conn = getConnection();
    final Statement stmt = conn.createStatement();
    stmt.executeUpdate("DELETE FROM " + table);
    stmt.close();
    conn.close();
  }

  List<List<Object>> getAll(String sql) throws Exception {
    final Connection conn = getConnection();
    final ResultSet rs = conn.createStatement().executeQuery(sql);
    final List<List<Object>> all = new ArrayList<List<Object>>();
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
