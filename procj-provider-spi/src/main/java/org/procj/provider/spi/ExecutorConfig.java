package org.procj.provider.spi;

public class ExecutorConfig {

  private boolean autoCommit;

  public ExecutorConfig(boolean autoCommit) {
    super();
    this.autoCommit = autoCommit;
  }

  public boolean isAutoCommit() {
    return autoCommit;
  }

  public static class Builder {

    private boolean autoCommit = true;

    public Builder autoCommit(boolean value) {
      this.autoCommit = value;
      return this;
    }

    public ExecutorConfig build() {
      return new ExecutorConfig(autoCommit);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (autoCommit ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ExecutorConfig other = (ExecutorConfig) obj;
    if (autoCommit != other.autoCommit) return false;
    return true;
  }
}
