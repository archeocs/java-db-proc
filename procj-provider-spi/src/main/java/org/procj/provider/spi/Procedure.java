package org.procj.provider.spi;

import java.util.Collection;
import java.util.Map;

public interface Procedure {
  void setParameterIn(int index, Object value) throws Exception;

  default Object getReturnValue() throws Exception {
    return all();
  }

  default Object getScalar() throws Exception {
    return first()[0];
  }

  void execute() throws Exception;

  Object[] first() throws Exception;

  Collection<Object[]> all() throws Exception;

  Map<String, ?> firstAsMap() throws Exception;

  Collection<Map<String, ?>> allAsMap() throws Exception;
}
