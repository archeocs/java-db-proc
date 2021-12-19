package org.procj.coerce;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ToInstantTest {

  private static final LocalDateTime DT = LocalDateTime.of(2021, 11, 12, 10, 55, 18);
  private static final Instant DT_I = DT.toInstant(UTC);
  private static final LocalDateTime DT_START = DT.truncatedTo(ChronoUnit.DAYS);
  private static final Instant DT_START_I = DT_START.toInstant(UTC);

  ToInstant ut = new ToInstant();

  @ParameterizedTest
  @MethodSource("paramsSource")
  public void shouldConvertToExpectedValue(Object input, Instant expected) {
    assertThat(ut.apply(input)).isEqualTo(expected);
  }

  static Stream<Arguments> paramsSource() {
    return Stream.of(
        Arguments.of(DT, DT_I),
        Arguments.of(DT.toLocalDate(), DT_START_I),
        Arguments.of(OffsetDateTime.of(DT, ZoneOffset.ofHours(2)), DT_I.minus(2, ChronoUnit.HOURS)),
        Arguments.of(Timestamp.from(DT_I), DT_I),
        Arguments.of(java.sql.Date.valueOf("2021-11-12"), DT_START_I),
        Arguments.of(java.util.Date.from(DT_I), DT_I),
        Arguments.of("2021-11-12T10:55:18Z", DT_I));
  }
}
