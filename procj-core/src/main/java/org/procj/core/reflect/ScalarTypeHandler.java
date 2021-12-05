package org.procj.core.reflect;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.procj.coerce.FromInstant;
import org.procj.coerce.ToInstant;

public abstract class ScalarTypeHandler {

  List<Class<?>> supportedTypes;

  public ScalarTypeHandler(Class<?>[] supportedTypes) {
    super();
    this.supportedTypes = Arrays.asList(supportedTypes);
  }

  public abstract Object convert(Object output);

  public boolean matches(Class<?> typeClass) {
    for (Class<?> c : supportedTypes) {
      if (c.equals(typeClass) || c.isAssignableFrom(typeClass)) {
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

  private static Instant asInstant(Object v) {
    if (v instanceof Instant) {
      return (Instant) v;
    } else if (v instanceof Timestamp) {
      return ((Timestamp) v).toInstant();
    } else if (v instanceof Date) {
      return asInstant(((Date) v).toLocalDate());
    } else if (v instanceof java.util.Date) {
      return ((java.util.Date) v).toInstant();
    } else if (v instanceof LocalDate) {
      return asInstant(((LocalDate) v).atStartOfDay());
    } else if (v instanceof LocalDateTime) {
      return ((LocalDateTime) v).toInstant(ZoneOffset.UTC);
    } else if (v instanceof OffsetDateTime) {
      return ((OffsetDateTime) v).toInstant();
    } else {
      return Instant.ofEpochMilli(asBigDecimal(v).longValue());
    }
  }

  private static <R> R asTemporal(Object v, Class<R> type, Function<Instant, R> convert) {
    if (v == null) {
      return null;
    } else if (type.isInstance(v)) {
      return type.cast(v);
    } else {
      return convert.apply(asInstant(v));
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

  public static ScalarTypeHandler getLocalDateTime() {
    return new FunctionalScalarTypeHander(new ToInstant().andThen(FromInstant::toLocalDateTime),
        LocalDateTime.class);
  }

  public static ScalarTypeHandler getLocalDate() {
    return new FunctionalScalarTypeHander(
        (v) ->
            asTemporal(
                v,
                LocalDate.class,
                (in) -> LocalDateTime.ofInstant(in, ZoneId.of("UTC")).toLocalDate()),
        LocalDate.class);
  }

  public static ScalarTypeHandler getOffsetDateTime() {
    return new FunctionalScalarTypeHander(
        (v) ->
            asTemporal(
                v, OffsetDateTime.class, (in) -> OffsetDateTime.ofInstant(in, ZoneId.of("UTC"))),
        OffsetDateTime.class);
  }
}
