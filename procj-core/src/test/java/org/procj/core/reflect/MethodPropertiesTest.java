package org.procj.core.reflect;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class MethodPropertiesTest {

  @Test
  public void shouldRecognizeHashCode() {
    MethodProperties ut = properties("hashCode");
    assertThat(ut.isHashCode()).isTrue();
  }

  @Test
  public void shouldRecognizeEquals() {
    MethodProperties ut = properties("equals", Object.class);
    assertThat(ut.isEquals()).isTrue();
  }

  @Test
  public void shouldRecognizeToString() {
    MethodProperties ut = properties("toString");
    assertThat(ut.isToString()).isTrue();
  }

  @Test
  public void shouldRecognizeCommit() {
    MethodProperties ut = properties("intMethod");
    assertThat(ut.isCommit()).isTrue();
  }

  @Test
  public void shouldRecognizeRollback() {
    MethodProperties ut = properties("setMethod");
    assertThat(ut.isRollback()).isTrue();
  }

  @Test
  public void shouldResolveProcedureName() {
    MethodProperties ut = properties("voidMethod");
    assertThat(ut.getProcedureName()).contains("test-procedure");
  }

  @SneakyThrows
  private MethodProperties properties(String m, Class<?>... params) {
    return new MethodProperties(SampleMethods.class.getMethod(m, params));
  }
}
