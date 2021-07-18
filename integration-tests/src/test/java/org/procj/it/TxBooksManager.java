package org.procj.it;

import org.procj.core.annotations.TxCommit;
import org.procj.core.annotations.TxRollback;

public interface TxBooksManager extends BooksManager {

  @TxCommit
  void commit();

  @TxRollback
  void rollback();
}
