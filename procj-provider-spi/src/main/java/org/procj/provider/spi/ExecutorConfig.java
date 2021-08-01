package org.procj.provider.spi;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ExecutorConfig {

  private boolean autoCommit;
}
