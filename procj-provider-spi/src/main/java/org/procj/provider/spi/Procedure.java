package org.procj.provider.spi;

import java.util.Collection;
import java.util.Map;

public interface Procedure {
  void setParameterIn(int index, Object value);

  Object getReturnValue();

  Object getScalar();

  Collection<?> getAll();

  void execute();
  
  /**
   * optional
   * @return
   */
  default Collection<Map<String, ?>> getAllMap() {
	  throw new UnsupportedOperationException("Reading result rows as map is not supported");
  }
  
  default boolean isRowsAsMapSupported() {
	  return false;
  }
}
