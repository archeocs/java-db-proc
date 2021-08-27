package org.procj.provider.spi;

import java.util.Collection;
import java.util.Map;

public interface Procedure {
  void setParameterIn(int index, Object value) throws Exception;

  Object getReturnValue() throws Exception;

  Object getScalar() throws Exception;

  Collection<?> getAll() throws Exception;

  void execute() throws Exception;

  /**
   * optional
   *
   * @return
   */
  default Collection<Map<String, ?>> allAsMap() throws Exception {
    throw new UnsupportedOperationException("Reading result rows as map is not supported");
  }

  default boolean isRowsAsMapSupported() {
    return false;
  }
}
