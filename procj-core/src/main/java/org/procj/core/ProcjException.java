package org.procj.core;

public class ProcjException extends RuntimeException {

  public ProcjException(String message, Throwable cause) {
    super(message, cause);
  }

  public ProcjException(String message) {
    super(message);
  }
}
