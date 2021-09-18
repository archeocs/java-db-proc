package org.procj.core.reflect;

import java.util.function.Function;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class ScalarTypeHandler {

  Class<?>[] supportedTypes;

  public abstract Object convert(Object output);

  public boolean matches(Class<?> typeClass) {
    for (Class<?> c : supportedTypes) {
      if (c.equals(typeClass)) {
        return true;
      }
    }
    return false;
  }

  private static class FunctionalScalarTypeHander extends ScalarTypeHandler {
    private final Function<Object, Object> convert;

    public FunctionalScalarTypeHander(
        Function<Object, Object> convert, Class<?>... supportedTypes) {
      super(supportedTypes);
      this.convert = convert;
    }

    @Override
    public Object convert(Object output) {
      return convert.apply(output);
    }
  }

  public static ScalarTypeHandler getString() {
    return new FunctionalScalarTypeHander(String::valueOf, String.class);
  }

  public static ScalarTypeHandler getObject() {
    return new FunctionalScalarTypeHander(Function.identity(), Object.class);
  }

  public static ScalarTypeHandler getBoolean() {
    return new FunctionalScalarTypeHander(Boolean.class::cast, Boolean.class, boolean.class);
  }

  public static ScalarTypeHandler getInteger() {
    return new FunctionalScalarTypeHander(Integer.class::cast, Integer.class, int.class);
  }

  public static ScalarTypeHandler getNumber() {
    return new FunctionalScalarTypeHander(Number.class::cast, Number.class);
  }
}
