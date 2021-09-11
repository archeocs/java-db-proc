package org.procj.core;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.procj.core.annotations.TxCommit;
import org.procj.provider.spi.ExecutorConfig;
import org.procj.provider.spi.ProcedureExecutor;
import org.procj.provider.spi.ProcedureExecutorProvider;
import org.procj.provider.spi.ProviderLoader;

@AllArgsConstructor
public class Procj {

  private final ProviderLoader loader;
  private static Procj INSTANCE;
  private Map<Object, ProcedureExecutor> executorsRegistry;

  @SuppressWarnings({"rawtypes", "unchecked"})
  private boolean hasMethodWithAnnotation(Class<?> cls, Class ann) {
    return Arrays.stream(cls.getMethods()).anyMatch(m -> m.getAnnotation(ann) != null);
  }

  private boolean hasCommitAnnotation(Class<?> cls) {
    return hasMethodWithAnnotation(cls, TxCommit.class);
  }

  @SuppressWarnings("unchecked")
  public <T> T create(Class<T> cls, String providerName, Properties configuration) {
    final ProcedureExecutorProvider provider = loader.getProvider(providerName);
    boolean isAutoCommit = !hasCommitAnnotation(cls);
    ProcedureExecutor executor =
        provider.initExecutor(
            configuration, ExecutorConfig.builder().autoCommit(isAutoCommit).build());
    T proxy =
        (T)
            Proxy.newProxyInstance(
                cls.getClassLoader(),
                new Class<?>[] {cls},
                new ProcjInvocationHandler(executor, UUID.randomUUID()));
    executorsRegistry.put(proxy, executor);
    return proxy;
  }

  public void release(Object o) {
    ProcedureExecutor executor = executorsRegistry.remove(o);
    if (executor != null) {
      try {
        executor.shutdown();
      } catch (Exception e) {
        throw new IllegalArgumentException("Releasing object failed", e);
      }
    }
  }

  public static Procj getInstance() {
    synchronized (Procj.class) {
      if (INSTANCE == null) {
        INSTANCE = new Procj(new ProviderLoader(), new HashMap<>());
      }
      return INSTANCE;
    }
  }
}
