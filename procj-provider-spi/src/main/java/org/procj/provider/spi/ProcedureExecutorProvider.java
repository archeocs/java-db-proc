package org.procj.provider.spi;

import java.util.Properties;

public interface ProcedureExecutorProvider {

  ProcedureExecutor initExecutor(Properties properties, ExecutorConfig config);

  String getName();
}
