package org.procj.provider.spi;

import java.util.Properties;

public interface ProcedureExecutorProvider {

  ProcedureExecutor initExecutor(String signature, Properties properties);

  String getName();
}
