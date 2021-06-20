package org.procj.provider.spi;

public interface ProcedureExecutor {
  Procedure getProcedure(String signature) throws Exception;

  void shutdown() throws Exception;

  void commit() throws Exception;

  void rollback() throws Exception;
}
