package org.procj.provider.spi;

public interface Procedure {
  void setParameterIn(int index, Object value);

  Object getReturnValue();

  void execute();
}
