package org.procj.provider.spi;

import java.util.Iterator;
import java.util.ServiceLoader;

public class ProviderLoader {

  private final ServiceLoader<ProcedureExecutorProvider> loader =
      ServiceLoader.load(ProcedureExecutorProvider.class);

  public ProcedureExecutorProvider getProvider(String name) {
    final Iterator<ProcedureExecutorProvider> it = loader.iterator();
    while (it.hasNext()) {
      final ProcedureExecutorProvider candidate = it.next();
      if (name.equals(candidate.getName())) {
        return candidate;
      }
    }
    throw new IllegalArgumentException(
        "Provider" + name + " not found. Check if provider implementation is present on classpath");
  }
}
