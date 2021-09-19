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
import lombok.RequiredArgsConstructor;
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
public class ProcjInvocationHandlerTest {

  @Mock ProcedureExecutor executor;

  @InjectMocks ProcjInvocationHandler underTest;

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

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void shouldExecuteProcedureAndReturnCollection() throws Throwable {
    final Object proxy = new Object();
    final TestProcedure procedure = new TestProcedure();
    when(executor.getProcedure("test-procedure-col")).thenReturn(procedure);

    final Object result =
        underTest.invoke(
            proxy, TestBundle.class.getMethod("testProcedureCol"), new Object[] {"t1"});
    assertThat((Collection) result)
        .containsAll(Collections.singletonList(Collections.singletonMap("id", "a")));
  }

  @Test
  public void shouldExecuteProcedureAndReturnPrimitiveNumber() throws Throwable {
    final Object proxy = new Object();
    final TestProcedure procedure = new TestProcedure(false, 100);
    when(executor.getProcedure("test-primitive")).thenReturn(procedure);

    final Object result =
        underTest.invoke(proxy, TestBundle.class.getMethod("testPrimitive"), new Object[] {});

    assertThat(result).isEqualTo(100);
  }

  @Test
  public void shouldExecuteProcedureAndConvertNullToPrimitive() throws Throwable {
    final Object proxy = new Object();
    final TestProcedure procedure = new TestProcedure(false, null);
    when(executor.getProcedure("test-primitive")).thenReturn(procedure);

    final Object result =
        underTest.invoke(proxy, TestBundle.class.getMethod("testPrimitive"), new Object[] {});

    assertThat(result).isEqualTo(0);
  }

  @Test
  public void shouldExecuteProcedureAndReturnBoxedNumber() throws Throwable {
    final Object proxy = new Object();
    final TestProcedure procedure = new TestProcedure(false, 100d);
    when(executor.getProcedure("test-boxed")).thenReturn(procedure);

    final Object result =
        underTest.invoke(proxy, TestBundle.class.getMethod("testBoxed"), new Object[] {});

    assertThat(result).isEqualTo(100L);
  }

  @Test
  public void shouldExecuteProcedureAndReturnNull() throws Throwable {
    final Object proxy = new Object();
    final TestProcedure procedure = new TestProcedure(false, null);
    when(executor.getProcedure("test-boxed")).thenReturn(procedure);

    final Object result =
        underTest.invoke(proxy, TestBundle.class.getMethod("testBoxed"), new Object[] {});

    assertThat(result).isNull();
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

    @ProcedureConfig(name = "test-primitive")
    int testPrimitive();

    @ProcedureConfig(name = "test-boxed")
    Long testBoxed();

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

  @RequiredArgsConstructor
  class TestProcedure implements Procedure {

    Map<Integer, Object> inParameters = new HashMap<Integer, Object>();

    private final boolean error;
    private final Object rv;

    public TestProcedure() {
      this(false, "return-value");
    }

    public TestProcedure(boolean error) {
      this(error, "return-value");
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
      return rv;
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
      return new Object[] {rv};
    }

    @Override
    public Map<String, ?> firstAsMap() throws Exception {
      return null;
    }
  }
}
