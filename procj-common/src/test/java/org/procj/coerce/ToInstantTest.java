package org.procj.coerce;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ToInstantTest {

  private static final LocalDateTime DT = LocalDateTime.of(2021, 11, 12, 10, 55, 18);

  ToInstant ut = new ToInstant();

  @ParameterizedTest
  @MethodSource("paramsSource")
  public void shouldConvertToExpectedValue(Object input, Instant expected) {
    assertThat(ut.apply(input)).isEqualTo(expected);
  }

  static Stream<Arguments> paramsSource() {
    return Stream.of(
        Arguments.of(DT, DT.toInstant(UTC)),
        Arguments.of(DT.toLocalDate(), DT.toLocalDate().atStartOfDay().toInstant(UTC)));
  }
}
