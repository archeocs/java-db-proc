package org.procj.core;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Properties;
import lombok.AllArgsConstructor;
import org.procj.core.annotations.TxCommit;
import org.procj.provider.spi.ExecutorConfig;
import org.procj.provider.spi.ProcedureExecutorProvider;
import org.procj.provider.spi.ProviderLoader;

@AllArgsConstructor
public class Procj {

  private final ProviderLoader loader;
  private static Procj INSTANCE;

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
    return (T)
        Proxy.newProxyInstance(
            cls.getClassLoader(),
            new Class<?>[] {cls},
            new ProcedureInvocationHandler(
                provider.initExecutor(
                    configuration, ExecutorConfig.builder().autoCommit(isAutoCommit).build())));
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
