package org.procj.provider.jdbc;

import java.util.Properties;

import org.procj.provider.spi.ProcedureExecutor;
import org.procj.provider.spi.ProcedureExecutorProvider;

public class JdbcProcedureExecutorProvider implements ProcedureExecutorProvider {

	@Override
	public ProcedureExecutor initExecutor(Properties properties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return "jdbc";
	}

}
