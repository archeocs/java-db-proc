package org.procj.it;

import org.procj.core.annotations.ProcedureConfig;

public interface BooksManager {

  @ProcedureConfig(name = "add_book")
  void addBook(String title);

  @ProcedureConfig(name = "count_books")
  Number countBooks();
}
