package org.procj.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.procj.core.annotations.Bundle;
import org.procj.core.annotations.ProcedureConfig;
import org.procj.provider.spi.ProcedureExecutor;
import org.procj.provider.spi.ProcedureExecutorProvider;
import org.procj.provider.spi.ProviderLoader;

@ExtendWith(MockitoExtension.class)
public class ProcjTest {

  @Mock ProviderLoader loader;

  @Mock ProcedureExecutorProvider provider;

  @Mock ProcedureExecutor executor;

  @InjectMocks Procj underTest;

  @Test
  public void shouldCreateBundle() {
    when(loader.getProvider("test-provider")).thenReturn(provider);
    final TestBundle bundle = underTest.create(TestBundle.class);

    assertThat(bundle).isNotNull();
    verify(provider).initExecutor(new Properties());
  }

  @Test
  public void shouldFailWhenBundleAnnotationIsMissing() {
    assertThatThrownBy(
            new ThrowableAssert.ThrowingCallable() {

              @Override
              public void call() throws Throwable {
                underTest.create(TestBundleWithoutAnnotation.class);
              }
            })
        .isInstanceOf(IllegalArgumentException.class);
  }

  interface TestBundleWithoutAnnotation {

    @ProcedureConfig(name = "test-procedure")
    void testProcedure();
  }

  @Bundle(
      provider = "test-provider",
      properties = {})
  interface TestBundle {

    @ProcedureConfig(name = "test-procedure")
    void testProcedure();
  }
}
