package org.procj.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.procj.core.annotations.ProcedureConfig;
import org.procj.core.annotations.TxCommit;
import org.procj.core.annotations.TxRollback;
import org.procj.provider.spi.Procedure;
import org.procj.provider.spi.ProcedureExecutor;

class ProcedureInvocationHandler implements InvocationHandler {

  private final ProcedureExecutor executor;

  public ProcedureInvocationHandler(ProcedureExecutor executor) {
    this.executor = executor;
  }

  private String resolveProcedureName(Method m) {
    final ProcedureConfig ann = m.getAnnotation(ProcedureConfig.class);
    return ann != null ? ann.name() : null;
  }

  private boolean isCommit(Method m) {
    return m.getAnnotation(TxCommit.class) != null;
  }

  private boolean isRollback(Method m) {
    return m.getAnnotation(TxRollback.class) != null;
  }

  private <T> T tryInvoke(ThrowableSupplier<T> fn, String failureMessage) {
    try {
      return fn.get();
    } catch (Exception e) {
      throw new ProcjException(failureMessage, e);
    }
  }

  private Object executeScalar(String name, Object[] args) throws Exception {
    final Procedure procedure = executor.getProcedure(name);
    if (args != null) {
      for (int i = 0; i < args.length; i++) {
        procedure.setParameterIn(i, args[i]);
      }
    }
    procedure.execute();
    return procedure.getScalar();
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (isCommit(method)) {
      return tryInvoke(
          () -> {
            executor.commit();
            return null;
          },
          "Commit");
    } else if (isRollback(method)) {
      return tryInvoke(
          () -> {
            executor.rollback();
            return null;
          },
          "Rollback");
    } else {
      final String name = resolveProcedureName(method);
      return tryInvoke(
          () -> {
            return executeScalar(name, args);
          },
          "Scalar value");
    }
  }
}
