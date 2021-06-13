package org.procj.provider.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.procj.provider.spi.Procedure;

public class JdbcProcedure implements Procedure {

  private final Connection connection;
  private Map<Integer, Object> parameters = new HashMap<>();
  private int maxParamIndex = 0;
  private ResultSet result;
  private PreparedStatement stmt;
  private final String name;

  public JdbcProcedure(String name, Connection connection) {
    this.connection = connection;
    this.name = name;
  }

  @Override
  public void setParameterIn(int index, Object value) {
    parameters.put(index, value);
    maxParamIndex = Math.max(maxParamIndex, index);
  }

  @Override
  public Object getReturnValue() {
    return getAll();
  }

  @Override
  public Object getScalar() {
    try {
      if (result.next()) {
        Object value = result.getObject(1);
        result.close();
        return value;
      }
      return null;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Collection<?> getAll() {
    if (result == null) {
      return null;
    }
    try {
      ResultSetMetaData meta = result.getMetaData();
      int cc = meta.getColumnCount();
      List<Object[]> rows = new ArrayList<>();
      while (result.next()) {
        Object[] row = new Object[cc];
        for (int i = 1; i <= cc; i++) {
          row[i - 1] = result.getObject(i);
        }
        rows.add(row);
      }
      result.close();
      return rows;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private String renderStatement() {
    StringBuilder builder = new StringBuilder();
    builder.append(" call ").append(name).append("(");
    String args =
        IntStream.range(0, maxParamIndex).mapToObj(i -> "?").collect(Collectors.joining(","));
    builder.append(args).append(")");
    return builder.toString();
  }

  @Override
  public void execute() {
    try {
      stmt = connection.prepareCall(renderStatement());
      for (int i = 1; i < maxParamIndex + 1; i++) {
        stmt.setObject(i, parameters.get(i));
      }
      boolean isRs = stmt.execute();
      if (isRs) {
        result = stmt.getResultSet();
      } else {
        stmt.getUpdateCount();
        stmt.close();
      }
      parameters.clear();
      maxParamIndex = 0;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
