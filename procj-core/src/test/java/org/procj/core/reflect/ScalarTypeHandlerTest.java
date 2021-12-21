package org.procj.core.reflect;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ScalarTypeHandlerTest {

  @ParameterizedTest
  @MethodSource("booleanConvert")
  public void shouldConvertValueToBoolean(Object source, Boolean output) {
    ScalarTypeHandler ut = ScalarTypeHandler.getBoolean();
    assertThat(ut.convert(source)).isEqualTo(output);
  }

  @ParameterizedTest
  @MethodSource("integerConvert")
  public void shouldValueToInteger(Object source, Integer output) {
    ScalarTypeHandler ut = ScalarTypeHandler.getInteger();
    assertThat(ut.convert(source)).isEqualTo(output);
  }

  @ParameterizedTest
  @MethodSource("longConvert")
  public void shouldValueToLong(Object source, Long output) {
    ScalarTypeHandler ut = ScalarTypeHandler.getLong();
    assertThat(ut.convert(source)).isEqualTo(output);
  }

  @ParameterizedTest
  @MethodSource("shortConvert")
  public void shouldValueToShort(Object source, Short output) {
    ScalarTypeHandler ut = ScalarTypeHandler.getShort();
    assertThat(ut.convert(source)).isEqualTo(output);
  }

  @ParameterizedTest
  @MethodSource("byteConvert")
  public void shouldValueToByte(Object source, Byte output) {
    ScalarTypeHandler ut = ScalarTypeHandler.getByte();
    assertThat(ut.convert(source)).isEqualTo(output);
  }

  @ParameterizedTest
  @MethodSource("numberConvert")
  public void shouldValueToNumber(Object source, Number output) {
    ScalarTypeHandler ut = ScalarTypeHandler.getNumber();
    assertThat(ut.convert(source)).isEqualTo(output);
  }

  @ParameterizedTest
  @MethodSource("bigDecimalConvert")
  public void shouldValueToNumber(Object source, BigDecimal output) {
    ScalarTypeHandler ut = ScalarTypeHandler.getBigDecimal();
    assertThat((BigDecimal) ut.convert(source)).isEqualByComparingTo(output);
  }

  @ParameterizedTest
  @MethodSource("localDateTimeConvert")
  public void shouldValueToLocalDateTime(Object source, LocalDateTime output) {
    ScalarTypeHandler ut = ScalarTypeHandler.getLocalDateTime();
    assertThat(ut.convert(source)).isEqualTo(output);
  }

  public static Object[][] integerConvert() {
    return new Object[][] {
      new Object[] {1, 1},
      new Object[] {1.02, 1},
      new Object[] {null, null},
      new Object[] {1d, 1},
      new Object[] {"1.0", 1},
      new Object[] {"1.023", 1},
    };
  }

  public static Object[][] byteConvert() {
    byte one = 0b1;
    return new Object[][] {
      new Object[] {1, one},
      new Object[] {1.02, one},
      new Object[] {null, null},
      new Object[] {1d, one},
      new Object[] {"1.0", one},
      new Object[] {"1.023", one},
    };
  }

  public static Object[][] shortConvert() {
    short one = 1;
    return new Object[][] {
      new Object[] {1, one},
      new Object[] {1.02, one},
      new Object[] {null, null},
      new Object[] {1d, one},
      new Object[] {"1.0", one},
      new Object[] {"1.023", one},
    };
  }

  public static Object[][] longConvert() {
    return new Object[][] {
      new Object[] {1, 1L},
      new Object[] {1.02, 1L},
      new Object[] {null, null},
      new Object[] {1d, 1L},
      new Object[] {"1.0", 1L},
      new Object[] {"1.023", 1L}
    };
  }

  public static Object[][] numberConvert() {
    return new Object[][] {
      new Object[] {1, 1},
      new Object[] {1.02, 1.02},
      new Object[] {null, null}
    };
  }

  public static Object[][] booleanConvert() {
    return new Object[][] {
      new Object[] {1, true},
      new Object[] {"false", false},
      new Object[] {1.02, false},
      new Object[] {null, null},
      new Object[] {"yes", true},
      new Object[] {1d, true},
      new Object[] {"1.0", true},
      new Object[] {"1.023", false},
      new Object[] {"any-string", false}
    };
  }

  public static Object[][] bigDecimalConvert() {
    return new Object[][] {
      new Object[] {1, BigDecimal.ONE},
      new Object[] {1.02, BigDecimal.valueOf(1.02d)},
      new Object[] {1d, BigDecimal.ONE},
      new Object[] {"1.0", BigDecimal.valueOf(1.0)},
      new Object[] {"1.023", BigDecimal.valueOf(1.023)},
    };
  }

  private static LocalDateTime ofEpochMili(long mili) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(mili), ZoneId.of("UTC"));
  }

  public static Object[][] localDateTimeConvert() {
    return new Object[][] {
      new Object[] {1, ofEpochMili(1)},
      new Object[] {1.02, ofEpochMili(1)},
      new Object[] {1d, ofEpochMili(1)},
      new Object[] {new Timestamp(1234), ofEpochMili(1234)},
      // because of limitations of java.sql.Date type, time component is lost
      new Object[] {new Date(1234), ofEpochMili(0)},
      new Object[] {new java.util.Date(1234), ofEpochMili(1234)},
    };
  }
}
