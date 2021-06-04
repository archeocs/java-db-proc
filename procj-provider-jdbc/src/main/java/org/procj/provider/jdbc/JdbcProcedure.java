package org.procj.provider.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.procj.provider.spi.Procedure;

public class JdbcProcedure implements Procedure {

	private final String name;
	private final Connection connection;
	private Map<Integer, Object> parameters = new HashMap<>();
	private int maxParamIndex = 0;
	
	public JdbcProcedure(String name, Connection connection) {
		super();
		this.name = name;
		this.connection = connection;
	}

	@Override
	public void setParameterIn(int index, Object value) {
		parameters.put(index, value);
		maxParamIndex = Math.max(maxParamIndex, index);
	}

	@Override
	public Object getReturnValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getScalar() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<?> getAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void execute() {
		try {
			CallableStatement stmt = connection.prepareCall(name);
			for (int i = 1; i < maxParamIndex + 1; i++) {
				stmt.setObject(i, parameters.get(i));
			}
			boolean isRs = stmt.execute();
			if (isRs) {
				stmt.getResultSet();
			} else {
				stmt.getUpdateCount();
			}
			stmt.close();
			parameters.clear();
			maxParamIndex = 0;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
