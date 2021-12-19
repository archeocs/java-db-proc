package org.procj.coerce;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class FromInstant {

  public static LocalDateTime toLocalDateTime(Instant i) {
    return LocalDateTime.ofInstant(i, ZoneId.of("UTC"));
  }

  public static LocalDate toLocalDate(Instant i) {
    return toLocalDateTime(i).toLocalDate();
  }

  public static OffsetDateTime toOffsetDateTime(Instant i) {
    return OffsetDateTime.ofInstant(i, ZoneId.of("UTC"));
  }

  // toUtilDate

  // toSqsDate

  // toTimestamp

  // toLocalDate

  // toOffsetDateTime

  // toInstant

}
