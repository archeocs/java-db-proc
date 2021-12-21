package org.procj.coerce;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ToNumberTest {

  ToNumber ut = new ToNumber();

  @ParameterizedTest
  @MethodSource("paramsSource")
  public void shouldConvertToExpectedValue(Object input, double expected) {
    assertThat(ut.apply(input).doubleValue()).isCloseTo(expected, offset(0.01));
  }

  static Stream<Arguments> paramsSource() {
    return Stream.of(
        Arguments.of("1", 1d),
        Arguments.of("0.1", 0.1d),
        Arguments.of(1999, 1999d),
        Arguments.of(BigDecimal.ONE, 1d),
        Arguments.of(123.4f, 123.4d),
        Arguments.of(112233L, 112233d));
  }
}
