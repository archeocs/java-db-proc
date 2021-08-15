package org.procj.provider.jdbc;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.procj.provider.spi.ExecutorConfig;
import org.procj.provider.spi.Procedure;
import org.procj.provider.spi.ProcedureExecutor;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public class JdbcProcedureExecutorProviderIT {
  public static final DockerImageName MYSQL_IMAGE = DockerImageName.parse("mysql:5.7.22");
  private static MySQLContainer<?> MYSQL;
  private ProcedureExecutor testExecutor;
  private JdbcProcedureExecutorProvider provider;
  private Properties conProps;

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
    provider = new JdbcProcedureExecutorProvider();
    conProps = new Properties();
    conProps.setProperty("jdbc.driver", MYSQL.getDriverClassName());
    conProps.setProperty("url", MYSQL.getJdbcUrl());
    conProps.setProperty("user", MYSQL.getUsername());
    conProps.setProperty("password", MYSQL.getPassword());
    conProps.setProperty("useSSL", "false");
  }

  @AfterEach
  public void shutDown() throws Exception {
    testExecutor.shutdown();
  }

  @AfterEach
  public void cleanDb() throws Exception {
    deleteAll("INPUT");
  }

  @Test
  public void shouldCallNoArgsProcedure() throws Exception {
    testExecutor = setupExecutor(true);
    final Procedure procedure = testExecutor.getProcedure("no_args");
    procedure.execute();

    final List<List<Object>> rows = getAll("SELECT * FROM INPUT");
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0)).contains("no-args");
    assertThat(procedure.getReturnValue()).isNull();
  }

  @Test
  public void shouldCallProcedureWithArgs() throws Exception {
    testExecutor = setupExecutor(true);
    final Procedure procedure = testExecutor.getProcedure("in_args");
    procedure.setParameterIn(1, "test_");
    procedure.setParameterIn(2, "value");
    procedure.execute();

    final List<List<Object>> rows = getAll("SELECT * FROM INPUT");
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0)).contains("test_value");
    assertThat(procedure.getReturnValue()).isNull();
  }

  @Test
  public void shouldCommitTransaction() throws Exception {
    testExecutor = setupExecutor(false);
    final Procedure procedure = testExecutor.getProcedure("no_args");
    procedure.execute();
    testExecutor.commit();

    final List<List<Object>> rows = getAll("SELECT * FROM INPUT");
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0)).contains("no-args");
    assertThat(procedure.getReturnValue()).isNull();
  }

  @Test
  public void shouldRollbackTransaction() throws Exception {
    testExecutor = setupExecutor(false);
    final Procedure procedure = testExecutor.getProcedure("no_args");
    procedure.execute();
    testExecutor.rollback();

    final List<List<Object>> rows = getAll("SELECT * FROM INPUT");
    assertThat(rows).hasSize(0);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldReadFromCursor() throws Exception {
    testExecutor = setupExecutor(true);
    final Procedure proc = testExecutor.getProcedure("get_src");
    proc.execute();
    final List<Object> rv = (List<Object>) proc.getReturnValue();
    List<Object[]> expected =
        asList(asList(1L, "A"), asList(2L, "B"), asList(3L, "C"), asList(4L, "D"), asList(5L, "E"))
            .stream()
            .map(e -> e.toArray())
            .collect(Collectors.toList());
    assertThat(rv).containsAll(expected);
  }

  @Test
  public void shouldReadFromCursorAsMap() throws Exception {
    testExecutor = setupExecutor(true);
    final Procedure proc = testExecutor.getProcedure("get_src");
    proc.execute();
    final Collection<Map<String, ?>> rv = proc.getAllMap();
    List<Map<String, ?>> expected = asList(mapRow("ID", 1L, "STR", "A"));
    assertThat(rv).hasSize(5);
    for (final Map<String, ?> e : expected) {
      assertThat(rv).contains(e);
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

  private ProcedureExecutor setupExecutor(boolean autoCommit) {
    return provider.initExecutor(conProps, ExecutorConfig.builder().autoCommit(autoCommit).build());
  }

  private Map<String, Object> mapRow(String k1, Object v1, String k2, Object v2) {
    HashMap<String, Object> map = new HashMap<>();
    map.put(k1, v1);
    map.put(k2, v2);
    return map;
  }
}
