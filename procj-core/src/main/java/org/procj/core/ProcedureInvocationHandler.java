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

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (isCommit(method)) {
      executor.commit();
      return null;
    } else if (isRollback(method)) {
      executor.rollback();
      return null;
    } else {
      final String name = resolveProcedureName(method);
      final Procedure procedure = executor.getProcedure(name);
      if (args != null) {
        for (int i = 0; i < args.length; i++) {
          procedure.setParameterIn(i, args[i]);
        }
      }
      procedure.execute();
      return procedure.getReturnValue();
    }
  }
}
