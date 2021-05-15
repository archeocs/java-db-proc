package org.procj.provider.spi;

public interface ProcedureExecutor {
  Procedure getProcedure(String signature);

  void shutdown();

  void commit();

  void rollback();
}
