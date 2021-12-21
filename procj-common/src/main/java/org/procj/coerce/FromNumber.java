package org.procj.coerce;

import java.math.BigDecimal;

public class FromNumber {

  public static int toInt(Number n) {
    return n.intValue();
  }

  public static Integer toInteger(Number n) {
    return n != null ? toInt(n) : null;
  }

  private static byte toByte(Number n) {
    return n.byteValue();
  }

  public static Byte toByteObject(Number n) {
    return n != null ? toByte(n) : null;
  }

  private static long toLong(Number n) {
    return n.longValue();
  }

  public static Long toLongObject(Number n) {
    return n != null ? toLong(n) : null;
  }

  private static short toShort(Number n) {
    return n.shortValue();
  }

  public static Short toShortObject(Number n) {
    return n != null ? toShort(n) : null;
  }

  public static BigDecimal toBigDecimal(Number n) {
    return n != null ? BigDecimal.valueOf(n.doubleValue()) : null;
  }
}
