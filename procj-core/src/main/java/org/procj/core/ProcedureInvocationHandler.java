package org.procj.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import lombok.AllArgsConstructor;
import org.procj.core.annotations.ProcedureConfig;
import org.procj.core.annotations.TxCommit;
import org.procj.core.annotations.TxRollback;
import org.procj.provider.spi.Procedure;
import org.procj.provider.spi.ProcedureExecutor;

@AllArgsConstructor
class ProcedureInvocationHandler implements InvocationHandler {

  private enum ReturnType {
    COLLECTION,
    MAP,
    VOID,
    OBJECT;
  }

  private final ProcedureExecutor executor;

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
    return prepareProcedure(name, args).getScalar();
  }

  private Object executeCollectionMap(String name, Object[] args) throws Exception {
    return prepareProcedure(name, args).getAllMap();
  }

  private Object executeCollection(String name, Object[] args) throws Exception {
    return prepareProcedure(name, args).getAll();
  }

  private Procedure prepareProcedure(String name, Object[] args) throws Exception {
    final Procedure procedure = executor.getProcedure(name);
    if (args != null) {
      for (int i = 0; i < args.length; i++) {
        procedure.setParameterIn(i + 1, args[i]);
      }
    }
    procedure.execute();
    return procedure;
  }

  private ReturnType resolveReturnType(Method m) {
    Class<?> cls = m.getReturnType();
    String cn = cls.getCanonicalName();
    if (cn.equals("void")) {
      return ReturnType.VOID;
    } else if (cn.equals("java.util.Collection")) {
      Type gt = m.getGenericReturnType();
      if (gt instanceof ParameterizedType) {
        ParameterizedType pt = (ParameterizedType) gt;
        Type at = pt.getActualTypeArguments()[0];
        if (at.getTypeName().startsWith("java.util.Map")) {
          return ReturnType.MAP;
        } else {
          return ReturnType.COLLECTION;
        }
      } else {
        return ReturnType.COLLECTION;
      }
    }
    return ReturnType.OBJECT;
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
      ReturnType rt = resolveReturnType(method);
      return tryInvoke(
          () -> {
            switch (rt) {
              case VOID:
              case OBJECT:
                return executeScalar(name, args);
              case COLLECTION:
                return executeCollection(name, args);
              case MAP:
                return executeCollectionMap(name, args);
              default:
                return executeScalar(name, args);
            }
          },
          "Scalar value");
    }
  }
}
