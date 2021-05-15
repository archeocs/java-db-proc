package org.procj.core;

import java.lang.reflect.Proxy;
import java.util.Properties;
import org.procj.core.annotations.Bundle;
import org.procj.provider.spi.ProcedureExecutorProvider;
import org.procj.provider.spi.ProviderLoader;

public class Procj {

  private final ProviderLoader loader;
  private static Procj INSTANCE;

  Procj(ProviderLoader loader) {
    this.loader = loader;
  }

  private String resolveProviderName(Class<?> cls) {
    final Bundle ann = cls.getAnnotation(Bundle.class);
    if (ann == null) {
      throw new IllegalArgumentException(
          "Annotation " + Bundle.class.getCanonicalName() + " is required");
    }
    return ann != null ? ann.provider() : null;
  }

  @SuppressWarnings("unchecked")
  public <T> T create(Class<T> cls) {
    final String name = resolveProviderName(cls);
    final ProcedureExecutorProvider provider = loader.getProvider(name);
    return (T)
        Proxy.newProxyInstance(
            cls.getClassLoader(),
            new Class<?>[] {cls},
            new ProcedureInvocationHandler(provider.initExecutor(new Properties())));
  }

  public static Procj getInstance() {
    synchronized (Procj.class) {
      if (INSTANCE == null) {
        INSTANCE = new Procj(new ProviderLoader());
      }
      return INSTANCE;
    }
  }
}
