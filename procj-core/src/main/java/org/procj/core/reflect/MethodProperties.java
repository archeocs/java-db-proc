package org.procj.core.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;
import lombok.Getter;
import org.procj.core.annotations.ProcedureConfig;
import org.procj.core.annotations.TxCommit;
import org.procj.core.annotations.TxRollback;

@Getter
public class MethodProperties {

  private final Method runtimeMethod;
  private final TypeProperties returnType;
  private final String name;

  public MethodProperties(Method runtimeMethod) {
    this.runtimeMethod = runtimeMethod;
    this.returnType = new TypeProperties(runtimeMethod.getGenericReturnType());
    this.name = runtimeMethod.getName();
  }

  public boolean isCommit() {
    return getAnnotation(runtimeMethod, TxCommit.class).isPresent();
  }

  public boolean isRollback() {
    return getAnnotation(runtimeMethod, TxRollback.class).isPresent();
  }

  public boolean isHashCode() {
    if (!name.equals("hashCode")) {
      return false;
    } else if (runtimeMethod.getParameterCount() > 0) {
      return false;
    } else {
      return returnType.isPrimitive() && returnType.isInteger();
    }
  }

  public boolean isToString() {
    if (!name.equals("toString")) {
      return false;
    } else if (runtimeMethod.getParameterCount() > 0) {
      return false;
    } else {
      return runtimeMethod.getReturnType().equals(String.class);
    }
  }

  public boolean isEquals() {
    if (!name.equals("equals")) {
      return false;
    } else if (!(returnType.isPrimitive() && returnType.isBoolean())) {
      return false;
    } else if (runtimeMethod.getParameterCount() != 1) {
      return false;
    } else {
      return runtimeMethod.getParameters()[0].getType().equals(Object.class);
    }
  }

  <T extends Annotation> Optional<T> getAnnotation(Method m, Class<T> annotation) {
    return Optional.ofNullable(m.getAnnotation(annotation));
  }

  public Optional<String> getProcedureName() {
    return getAnnotation(runtimeMethod, ProcedureConfig.class).map(ProcedureConfig::name);
  }
}
