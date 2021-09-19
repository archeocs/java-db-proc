package org.procj.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.procj.core.reflect.MethodProperties;
import org.procj.core.reflect.TypeProperties;
import org.procj.provider.spi.Procedure;
import org.procj.provider.spi.ProcedureExecutor;

@AllArgsConstructor
public class ProcjInvocationHandler implements InvocationHandler {

  private final ProcedureExecutor executor;
  private final UUID handlerId;

  private <T> T tryInvoke(ThrowableSupplier<T> fn, String failureMessage) {
    try {
      return fn.get();
    } catch (Exception e) {
      throw new ProcjException(failureMessage, e);
    }
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    MethodProperties mp = new MethodProperties(method);
    if (mp.isHashCode()) {
      return handlerId.hashCode();
    } else if (mp.isEquals()) {
      return handlerId.equals(args[0]);
    } else if (mp.isToString()) {
      return getClass().getSimpleName() + "#" + handlerId.toString();
    } else if (mp.isCommit()) {
      return tryInvoke(
          () -> {
            executor.commit();
            return null;
          },
          "Commit");
    } else if (mp.isRollback()) {
      tryInvoke(
          () -> {
            executor.rollback();
            return null;
          },
          "Rollback");
    } else {
      Optional<String> name = mp.getProcedureName();
      if (name.isPresent()) {
        return tryInvoke(
            () -> invokeProcedure(mp.getReturnType(), name.get(), args), "Procedure Invocation");
      }
    }
    return null;
  }

  private Object invokeProcedure(TypeProperties returnType, String name, Object[] args)
      throws Exception {
    Procedure p = executor.getProcedure(name);
    if (args != null) {
      for (int i = 0; i < args.length; i++) {
        p.setParameterIn(i + 1, args[i]);
      }
    }
    p.execute();
    if (returnType.isVoid()) {
      return null;
    } else if (returnType.isMap()) {
      return p.firstAsMap();
    } else if (returnType.isList() || returnType.isCollection()) {
      return p.allAsMap().stream().collect(Collectors.toList());
    } else if (returnType.isSet()) {
      return p.allAsMap().stream().collect(Collectors.toSet());
    } else if (returnType.isScalar()) {
      Object output = returnType.getScalarConverter().convert(p.first()[0]);
      if (output == null) {
        return convertNull(returnType);
      }
      return output;
    }
    return null;
  }

  private static Object convertNull(TypeProperties type) {
    if (!type.isPrimitive()) {
      return null;
    } else if (type.isNumeric()) {
      return 0;
    } else {
      return false;
    }
  }
}
