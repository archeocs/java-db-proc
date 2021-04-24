package org.procj.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.procj.core.annotations.ProcedureConfig;
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

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    final String name = resolveProcedureName(method);
    final Procedure procedure = executor.getProcedure(name);
    for (int i = 0; i < args.length; i++) {
      procedure.setParameterIn(i, args[i]);
    }
    procedure.execute();
    return procedure.getReturnValue();
  }
}
