package org.procj.core.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.procj.core.ProcjException;

@Getter
public class TypeProperties {

  private final Type runtimeType;

  private final Class<?> typeClass;

  TypeProperties(Type runtimeType) {
    if (runtimeType instanceof Class) {
      this.runtimeType = runtimeType;
      typeClass = (Class<?>) runtimeType;
    } else if (runtimeType instanceof ParameterizedType) {
      this.runtimeType = runtimeType;
      typeClass = (Class<?>) ((ParameterizedType) runtimeType).getRawType();
    } else {
      throw new ProcjException(runtimeType.getClass().getCanonicalName() + " is not supported");
    }
  }

  public boolean isVoid() {
    return typeClass == Void.TYPE;
  }

  public boolean isList() {
    return List.class.isAssignableFrom(typeClass);
  }

  public boolean isCollection() {
    return Collection.class.isAssignableFrom(typeClass);
  }

  public boolean isSet() {
    return Set.class.isAssignableFrom(typeClass);
  }

  public boolean isMap() {
    return Map.class.isAssignableFrom(typeClass);
  }

  public boolean isObject() {
    return typeClass.isAssignableFrom(Object.class);
  }

  public boolean isPrimitive() {
    return typeClass.isPrimitive();
  }

  public boolean isString() {
    return typeClass.isAssignableFrom(String.class);
  }

  private boolean matchesTypes(Class<?>... other) {
    return Arrays.stream(other).filter(o -> o.isAssignableFrom(typeClass)).count() > 0;
  }

  public boolean isNumber() {
    return matchesTypes(Number.class);
  }

  public boolean isInteger() {
    return matchesTypes(Integer.class, int.class);
  }

  public boolean isBoolean() {
    return matchesTypes(Boolean.class, boolean.class);
  }
}
