package org.procj.core.reflect;

import static org.procj.core.reflect.ScalarTypeHandler.*;
import static org.procj.core.reflect.ScalarTypeHandler.getBoolean;
import static org.procj.core.reflect.ScalarTypeHandler.getInteger;
import static org.procj.core.reflect.ScalarTypeHandler.getNumber;
import static org.procj.core.reflect.ScalarTypeHandler.getObject;
import static org.procj.core.reflect.ScalarTypeHandler.getString;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.procj.core.ProcjException;

@Getter
public class TypeProperties {

  private static final ScalarTypeHandler[] SCALARS = {getBoolean(), getString(), getObject()};

  private static final ScalarTypeHandler[] NUMERIC_SCALARS = {
    getInteger(), getShort(), getLong(), getByte(), getBigDecimal(), getNumber()
  };

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

  public boolean isPrimitive() {
    return typeClass.isPrimitive();
  }

  public boolean isNumeric() {
    for (ScalarTypeHandler sc : NUMERIC_SCALARS) {
      if (sc.matches(typeClass)) {
        return true;
      }
    }
    return false;
  }

  public boolean matches(Class<?> type) {
    return getScalarConverter().matches(type);
  }

  public ScalarTypeHandler getScalarConverter() {
    for (ScalarTypeHandler sc : NUMERIC_SCALARS) {
      if (sc.matches(typeClass)) {
        return sc;
      }
    }
    for (ScalarTypeHandler sc : SCALARS) {
      if (sc.matches(typeClass)) {
        return sc;
      }
    }
    return getObject();
  }

  public boolean isScalar() {
    for (ScalarTypeHandler sc : NUMERIC_SCALARS) {
      if (sc.matches(typeClass)) {
        return true;
      }
    }
    for (ScalarTypeHandler sc : SCALARS) {
      if (sc.matches(typeClass)) {
        return true;
      }
    }
    return false;
  }
}
