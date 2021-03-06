package org.procj.it;

import java.util.Collection;
import java.util.Map;
import org.procj.core.annotations.ProcedureConfig;
import org.procj.core.annotations.TxCommit;
import org.procj.core.annotations.TxRollback;

public interface TxBooksManager {

  @ProcedureConfig(name = "add_book")
  void addBook(String title, int score);

  @ProcedureConfig(name = "count_books")
  Number countBooks();

  @ProcedureConfig(name = "search_books")
  Collection<Map<String, Object>> searchBooks(String prefix);

  @TxCommit
  void commit();

  @TxRollback
  void rollback();
}
