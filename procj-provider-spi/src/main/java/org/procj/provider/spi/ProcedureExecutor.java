package org.procj.provider.spi;

public interface ProcedureExecutor {

  void setParameterIn(int index, Object value);

  Object getReturnValue();

  void execute();
}
