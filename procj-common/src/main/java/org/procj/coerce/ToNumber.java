package org.procj.coerce;

import java.math.BigDecimal;
import java.util.function.Function;

public class ToNumber implements Function<Object, Number> {

  private Number from(String s) {
    return new BigDecimal(s);
  }

  @Override
  public Number apply(Object t) {
    if (t == null) {
      return null;
    } else if (t instanceof Number) {
      return (Number) t;
    } else {
      return from(t.toString());
    }
  }
}
