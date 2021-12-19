package org.procj.coerce;

import static java.time.ZoneOffset.UTC;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.function.Function;

public class ToInstant implements Function<Object, Instant> {

  private Instant from(LocalDate d) {
    return from(d.atStartOfDay());
  }

  private Instant from(LocalDateTime d) {
    return d.toInstant(UTC);
  }

  private Instant from(OffsetDateTime d) {
    return d.toInstant();
  }

  private Instant from(Timestamp d) {
    return d.toInstant();
  }

  private Instant fromDate(Date d) {
    return d.toInstant();
  }

  private Instant from(java.sql.Date d) {
    return from(d.toLocalDate());
  }

  private Instant from(Number n) {
    return Instant.ofEpochMilli(n.longValue());
  }

  private Instant from(String s) {
    return Instant.parse(s);
  }

  @Override
  public Instant apply(Object t) {
    if (t == null) {
      return null;
    } else if (t instanceof Instant) {
      return (Instant) t;
    } else if (t instanceof LocalDate) {
      return from((LocalDate) t);
    } else if (t instanceof LocalDateTime) {
      return from((LocalDateTime) t);
    } else if (t instanceof OffsetDateTime) {
      return from((OffsetDateTime) t);
    } else if (t instanceof Timestamp) {
      return from((Timestamp) t);
    } else if (t instanceof java.sql.Date) {
      return from((java.sql.Date) t);
    } else if (t instanceof Date) {
      return fromDate((Date) t);
    } else if (t instanceof Number) {
      return from((Number) t);
    } else if (t instanceof String) {
      return from((String) t);
    } else {
      return from(t.toString());
    }
  }
}
