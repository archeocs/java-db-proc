package org.procj.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.procj.core.annotations.ProcedureConfig;
import org.procj.core.annotations.TxCommit;
import org.procj.core.annotations.TxRollback;
import org.procj.provider.spi.Procedure;
import org.procj.provider.spi.ProcedureExecutor;
import org.procj.provider.spi.ProcedureExecutorProvider;
import org.procj.provider.spi.ProviderLoader;

@ExtendWith(MockitoExtension.class)
public class ProcjTest {

  @Mock ProviderLoader loader;

  @Mock ProcedureExecutorProvider provider;

  @Mock ProcedureExecutor executor;

  @Mock Procedure procedure;

  @InjectMocks Procj underTest;

  @Test
  public void shouldCreateBundle() {
    Properties expectedProps = new Properties();
    expectedProps.setProperty("config-prop", "config-value");
    when(loader.getProvider("test-provider")).thenReturn(provider);
    final TestBundle bundle = underTest.create(TestBundle.class, "test-provider", expectedProps);

    assertThat(bundle).isNotNull();

    verify(provider).initExecutor(expectedProps);
  }

  @Test
  public void shouldExecuteProcedure() throws Exception {
    String expectedReturn = UUID.randomUUID().toString();
    setupExecutor(expectedReturn);

    TestBundle bundle = underTest.create(TestBundle.class, "test-provider", new Properties());
    String result = bundle.testProcedure();
    assertThat(result).isEqualTo(expectedReturn);
  }

  @Test
  public void shouldCommitTx() throws Exception {
    setupExecutor(null);

    TestBundle bundle = underTest.create(TestBundle.class, "test-provider", new Properties());
    bundle.testCommit();

    verify(executor).commit();
  }

  @Test
  public void shouldRollbackTx() throws Exception {
    setupExecutor(null);

    TestBundle bundle = underTest.create(TestBundle.class, "test-provider", new Properties());
    bundle.testRollback();

    verify(executor).rollback();
  }

  private void setupExecutor(String returnValue) throws Exception {
    when(loader.getProvider("test-provider")).thenReturn(provider);
    when(provider.initExecutor(any())).thenReturn(executor);
    if (returnValue != null) {
      when(executor.getProcedure("test-procedure")).thenReturn(procedure);
      when(procedure.getScalar()).thenReturn(returnValue);
    }
  }

  interface TestBundle {

    @ProcedureConfig(name = "test-procedure")
    String testProcedure();

    @TxCommit
    void testCommit();

    @TxRollback
    void testRollback();
  }
}
