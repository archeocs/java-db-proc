package org.procj.core;

import java.lang.reflect.Proxy;
import java.util.Properties;
import org.procj.core.annotations.Bundle;
import org.procj.core.annotations.ConfigProperty;
import org.procj.provider.spi.ProcedureExecutorProvider;
import org.procj.provider.spi.ProviderLoader;

public class Procj {

  private final ProviderLoader loader;
  private static Procj INSTANCE;

  Procj(ProviderLoader loader) {
    this.loader = loader;
  }

  private Bundle getBundle(Class<?> cls) {
    final Bundle ann = cls.getAnnotation(Bundle.class);
    if (ann == null) {
      throw new IllegalArgumentException(
          "Annotation " + Bundle.class.getCanonicalName() + " is required");
    }
    return ann;
  }

  private Properties executorProperties(Bundle bundle) {
    Properties props = new Properties();
    for (ConfigProperty cfg : bundle.properties()) {
      props.setProperty(cfg.name(), cfg.value());
    }
    return props;
  }

  @SuppressWarnings("unchecked")
  public <T> T create(Class<T> cls) {
    Bundle bundle = getBundle(cls);
    final ProcedureExecutorProvider provider = loader.getProvider(bundle.provider());
    return (T)
        Proxy.newProxyInstance(
            cls.getClassLoader(),
            new Class<?>[] {cls},
            new ProcedureInvocationHandler(provider.initExecutor(executorProperties(bundle))));
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
