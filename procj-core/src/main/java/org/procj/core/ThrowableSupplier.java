package org.procj.core;

@FunctionalInterface
public interface ThrowableSupplier<T> {
  T get() throws Exception;
}
