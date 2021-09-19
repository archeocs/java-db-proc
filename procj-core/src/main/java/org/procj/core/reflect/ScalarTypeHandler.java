package org.procj.core.reflect;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public abstract class ScalarTypeHandler {

  List<Class<?>> supportedTypes;

  public ScalarTypeHandler(Class<?>[] supportedTypes) {
    super();
    this.supportedTypes = Arrays.asList(supportedTypes);
  }

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

  private static BigDecimal asBigDecimal(Object v) {
    if (v instanceof BigDecimal) {
      return (BigDecimal) v;
    } else if (v instanceof Number) {
      return BigDecimal.valueOf(((Number) v).doubleValue());
    } else if (v instanceof Boolean) {
      return Boolean.TRUE.equals(v) ? BigDecimal.ONE : BigDecimal.ZERO;
    } else {
      try {
        return new BigDecimal(v.toString());
      } catch (NumberFormatException e) {
        return "yes".equalsIgnoreCase(v.toString()) ? BigDecimal.ONE : BigDecimal.ZERO;
      }
    }
  }

  @SuppressFBWarnings("NP_BOOLEAN_RETURN_NULL")
  private static Boolean asBoolean(Object v) {
    if (v == null) {
      return null;
    }
    if (v instanceof Boolean) {
      return (Boolean) v;
    }
    boolean parsed = Boolean.parseBoolean(v.toString());
    if (parsed) {
      return true;
    } else {
      return asBigDecimal(v).compareTo(BigDecimal.ONE) == 0;
    }
  }

  private static Number asNumber(Object v) {
    if (v == null) {
      return null;
    } else if (Number.class.isAssignableFrom(v.getClass())) {
      return (Number) v;
    } else {
      return asBigDecimal(v);
    }
  }

  private static <R> R asNumeric(Object v, Class<R> type, Function<BigDecimal, R> convert) {
    if (v == null) {
      return null;
    } else if (type.isInstance(v)) {
      return type.cast(v);
    } else {
      return convert.apply(asBigDecimal(v));
    }
  }

  public static ScalarTypeHandler getString() {
    return new FunctionalScalarTypeHander(String::valueOf, String.class);
  }

  public static ScalarTypeHandler getObject() {
    return new FunctionalScalarTypeHander(Function.identity(), Object.class);
  }

  public static ScalarTypeHandler getBoolean() {
    return new FunctionalScalarTypeHander(
        ScalarTypeHandler::asBoolean, Boolean.class, boolean.class);
  }

  public static ScalarTypeHandler getInteger() {
    return new FunctionalScalarTypeHander(
        (v) -> asNumeric(v, Integer.class, BigDecimal::intValue), Integer.class, int.class);
  }

  public static ScalarTypeHandler getLong() {
    return new FunctionalScalarTypeHander(
        (v) -> asNumeric(v, Long.class, BigDecimal::longValue), Long.class, long.class);
  }

  public static ScalarTypeHandler getShort() {
    return new FunctionalScalarTypeHander(
        (v) -> asNumeric(v, Short.class, BigDecimal::shortValue), Short.class, short.class);
  }

  public static ScalarTypeHandler getByte() {
    return new FunctionalScalarTypeHander(
        (v) -> asNumeric(v, Byte.class, BigDecimal::byteValue), Byte.class, byte.class);
  }

  public static ScalarTypeHandler getNumber() {
    return new FunctionalScalarTypeHander(ScalarTypeHandler::asNumber, Number.class);
  }

  public static ScalarTypeHandler getBigDecimal() {
    return new FunctionalScalarTypeHander(ScalarTypeHandler::asBigDecimal, BigDecimal.class);
  }
}
