package org.procj.core;

import java.lang.reflect.Proxy;
import java.util.Properties;
import org.procj.provider.spi.ProcedureExecutorProvider;
import org.procj.provider.spi.ProviderLoader;

public class Procj {

  private final ProviderLoader loader;
  private static Procj INSTANCE;

  Procj(ProviderLoader loader) {
    this.loader = loader;
  }

  @SuppressWarnings("unchecked")
  public <T> T create(Class<T> cls, String providerName, Properties configuration) {
    final ProcedureExecutorProvider provider = loader.getProvider(providerName);
    return (T)
        Proxy.newProxyInstance(
            cls.getClassLoader(),
            new Class<?>[] {cls},
            new ProcedureInvocationHandler(provider.initExecutor(configuration)));
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
