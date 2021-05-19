package org.procj.provider.spi;

import java.util.Collection;

public interface Procedure {
  void setParameterIn(int index, Object value);

  Object getReturnValue();

  Object getScalar();

  Collection<?> getAll();

  void execute();
}
