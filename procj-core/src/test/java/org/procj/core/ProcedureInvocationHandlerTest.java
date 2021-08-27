package org.procj.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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

@ExtendWith(MockitoExtension.class)
public class ProcedureInvocationHandlerTest {

  @Mock ProcedureExecutor executor;

  @InjectMocks ProcedureInvocationHandler underTest;

  @Test
  public void shouldCreateAndExecuteProcedure() throws Throwable {
    final Object proxy = new Object();
    final TestProcedure procedure = new TestProcedure();
    when(executor.getProcedure("test-procedure")).thenReturn(procedure);

    final Object result =
        underTest.invoke(proxy, TestBundle.class.getMethod("testProcedure"), new Object[] {"t1"});
    assertThat(result).isNull();
    assertThat(procedure.inParameters).containsEntry(1, "t1");
  }

  @Test
  public void shouldExecuteProcedureAndReturnMap() throws Throwable {
    final Object proxy = new Object();
    final TestProcedure procedure = new TestProcedure();
    when(executor.getProcedure("test-procedure-map")).thenReturn(procedure);

    final Object result =
        underTest.invoke(
            proxy, TestBundle.class.getMethod("testProcedureMap"), new Object[] {"t1"});
    assertThat(result).isEqualTo(Arrays.asList(Collections.singletonMap("id", "a")));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldExecuteProcedureAndReturnCollection() throws Throwable {
    final Object proxy = new Object();
    final TestProcedure procedure = new TestProcedure();
    when(executor.getProcedure("test-procedure-col")).thenReturn(procedure);

    final Object result =
        underTest.invoke(
            proxy, TestBundle.class.getMethod("testProcedureCol"), new Object[] {"t1"});
    assertThat((Collection) result)
        .containsAll(Collections.singletonList(new Object[] {"a", "b", "c"}));
  }

  @Test
  public void shouldCommitTx() throws Throwable {
    final Object proxy = new Object();
    underTest.invoke(proxy, TestBundle.class.getMethod("testCommit"), null);

    verify(executor).commit();
  }

  @Test
  public void shouldRollbackTx() throws Throwable {
    final Object proxy = new Object();
    underTest.invoke(proxy, TestBundle.class.getMethod("testRollback"), null);

    verify(executor).rollback();
  }

  @Test
  public void shouldSwallowExceptionAndThrowRuntimeError() throws Exception {
    final Object proxy = new Object();
    final TestProcedure procedure = new TestProcedure(true);
    when(executor.getProcedure("test-procedure")).thenReturn(procedure);
    assertThrows(
        ProcjException.class,
        () -> {
          underTest.invoke(proxy, TestBundle.class.getMethod("testProcedure"), null);
        });
  }

  interface TestBundle {

    @ProcedureConfig(name = "test-procedure")
    void testProcedure();

    @ProcedureConfig(name = "test-procedure-obj")
    Object testProcedureObj();

    @SuppressWarnings("rawtypes")
    @ProcedureConfig(name = "test-procedure-map")
    Collection<Map> testProcedureMap();

    @SuppressWarnings("rawtypes")
    @ProcedureConfig(name = "test-procedure-col")
    Collection testProcedureCol();

    @TxCommit
    void testCommit();

    @TxRollback
    void testRollback();
  }

  class TestProcedure implements Procedure {

    Map<Integer, Object> inParameters = new HashMap<Integer, Object>();

    private boolean error;

    public TestProcedure() {
      this(false);
    }

    public TestProcedure(boolean error) {
      this.error = error;
    }

    @Override
    public void setParameterIn(int index, Object value) throws Exception {
      if (error) {
        throw new Exception("Set parameter");
      }
      inParameters.put(index, value);
    }

    @Override
    public Object getReturnValue() throws Exception {
      if (error) {
        throw new Exception("Get return");
      }
      return "return-value";
    }

    @Override
    public void execute() throws Exception {
      if (error) {
        throw new Exception("Execute");
      }
    }

    @Override
    public Object getScalar() throws Exception {
      if (error) {
        throw new Exception("Get scalar");
      }
      return "return-value";
    }

    @Override
    public Collection<Object[]> all() throws Exception {
      if (error) {
        throw new Exception("Get all");
      }
      return Collections.singletonList(new Object[] {"a", "b", "c"});
    }

    @Override
    public Collection<Map<String, ?>> allAsMap() throws Exception {
      if (error) {
        throw new Exception("Get map");
      }
      Map<String, String> row = Collections.singletonMap("id", "a");
      return Arrays.asList(row);
    }

    @Override
    public Object[] first() throws Exception {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Map<String, ?> firstAsMap() throws Exception {
      // TODO Auto-generated method stub
      return null;
    }
  }
}
