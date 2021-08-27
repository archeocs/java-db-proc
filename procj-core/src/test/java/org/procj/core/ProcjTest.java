package org.procj.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.procj.core.annotations.ProcedureConfig;
import org.procj.core.annotations.TxCommit;
import org.procj.core.annotations.TxRollback;
import org.procj.provider.spi.ExecutorConfig;
import org.procj.provider.spi.Procedure;
import org.procj.provider.spi.ProcedureExecutor;
import org.procj.provider.spi.ProcedureExecutorProvider;
import org.procj.provider.spi.ProviderLoader;

@ExtendWith(MockitoExtension.class)
public class ProcjTest {

  private static final ExecutorConfig AUTOCOMMIT_DISABLED_CFG =
      ExecutorConfig.builder().autoCommit(false).build();

  @Mock ProviderLoader loader;

  @Mock ProcedureExecutorProvider provider;

  @Mock ProcedureExecutor executor;

  @Mock Procedure procedure;

  private HashMap<Object, ProcedureExecutor> executors;

  Procj underTest;

  @BeforeEach
  public void setupProcJ() {
    executors = new HashMap<>();
    underTest = new Procj(loader, executors);
  }

  @Test
  public void shouldCreateBundle() {
    Properties expectedProps = new Properties();
    expectedProps.setProperty("config-prop", "config-value");
    when(loader.getProvider("test-provider")).thenReturn(provider);
    final TestBundle bundle = underTest.create(TestBundle.class, "test-provider", expectedProps);

    assertThat(bundle).isNotNull();

    verify(provider).initExecutor(expectedProps, AUTOCOMMIT_DISABLED_CFG);
  }

  @Test
  public void shouldCreateBundleWhenExtendedInterface() {
    Properties expectedProps = new Properties();
    expectedProps.setProperty("config-prop", "config-value");
    when(loader.getProvider("test-provider")).thenReturn(provider);
    final TxBundleExtended bundle =
        underTest.create(TxBundleExtended.class, "test-provider", expectedProps);

    assertThat(bundle).isNotNull();

    verify(provider).initExecutor(expectedProps, AUTOCOMMIT_DISABLED_CFG);
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

  @Test
  public void shouldCreateBudleWithAutoCommit() {
    Properties expectedProps = new Properties();
    expectedProps.setProperty("config-prop", "config-value");
    when(loader.getProvider("test-provider")).thenReturn(provider);
    final TestBundleAutoCommit bundle =
        underTest.create(TestBundleAutoCommit.class, "test-provider", expectedProps);

    assertThat(bundle).isNotNull();
    verify(provider).initExecutor(expectedProps, ExecutorConfig.builder().autoCommit(true).build());
  }

  @Test
  public void shouldCommitTxWhenExtendingInterface() throws Exception {
    setupExecutor(null);

    TxBundleExtended bundle =
        underTest.create(TxBundleExtended.class, "test-provider", new Properties());
    bundle.testCommit();

    verify(executor).commit();
  }

  @Test
  public void shouldShutdownRelatedExecutor() throws Exception {
    when(loader.getProvider("test-provider")).thenReturn(provider);
    ProcedureExecutor e1 = mock(ProcedureExecutor.class);
    ProcedureExecutor e2 = mock(ProcedureExecutor.class);

    Properties p1 = new Properties();
    p1.put("e", "1");
    Properties p2 = new Properties();
    p2.put("e", "2");

    when(provider.initExecutor(eq(p1), any())).thenReturn(e1);
    when(provider.initExecutor(eq(p2), any())).thenReturn(e2);

    TxBundleExtended b1 = underTest.create(TxBundleExtended.class, "test-provider", p1);
    TxBundleExtended b2 = underTest.create(TxBundleExtended.class, "test-provider", p2);

    assertThat(executors).containsEntry(b1, e1).containsEntry(b2, e2);

    underTest.release(b2);
    assertThat(executors).doesNotContainEntry(b2, e2).containsEntry(b1, e1);
    verify(e2).shutdown();

    underTest.release(b1);
    assertThat(executors).isEmpty();
    verify(e1).shutdown();
  }

  @Test
  public void shouldReleaseProxy() throws Exception {
    setupExecutor(null);
    TxBundleExtended bundle =
        underTest.create(TxBundleExtended.class, "test-provider", new Properties());
    assertThat(executors).containsEntry(bundle, executor);

    underTest.release(bundle);
    assertThat(executors).isEmpty();
    verify(executor).shutdown();
  }

  private void setupExecutor(String returnValue) throws Exception {
    when(loader.getProvider("test-provider")).thenReturn(provider);
    when(provider.initExecutor(any(), any())).thenReturn(executor);
    if (returnValue != null) {
      when(executor.getProcedure("test-procedure")).thenReturn(procedure);
      when(procedure.getScalar()).thenReturn(returnValue);
    }
  }

  interface TestBundleAutoCommit {
    @ProcedureConfig(name = "test-procedure")
    String testProcedure();
  }

  interface TxBundleExtended extends TestBundleAutoCommit {
    @TxCommit
    void testCommit();
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
