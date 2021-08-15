package org.procj.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.procj.core.Procj;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public class ProcjIT {

  public static final DockerImageName MYSQL_IMAGE = DockerImageName.parse("mysql:5.7.22");
  private static MySQLContainer<?> MYSQL;
  private TxBooksManager manager;

  @SuppressWarnings({"resource", "rawtypes"})
  @BeforeAll
  public static void setupConainer() {
    MYSQL = (MySQLContainer<?>) new MySQLContainer(MYSQL_IMAGE).withInitScript("init-db.sql");
    MYSQL.start();
  }

  @AfterAll
  public static void shutodown() {
    MYSQL.stop();
  }

  @BeforeEach
  public void intManager() {
    Properties config = new Properties();
    config.setProperty("jdbc.driver", MYSQL.getDriverClassName());
    config.setProperty("url", MYSQL.getJdbcUrl());
    config.setProperty("user", MYSQL.getUsername());
    config.setProperty("password", MYSQL.getPassword());
    config.setProperty("useSSL", "false");
    manager = Procj.getInstance().create(TxBooksManager.class, "jdbc", config);
  }

  @AfterEach
  public void rollbackManager() {
    manager.rollback();
  }

  @Test
  public void shouldAddBookToDatabase() {

    Number countBefore = manager.countBooks();

    manager.addBook("Test book", 1);

    Number countAfter = manager.countBooks();

    assertThat(countAfter).isEqualTo(countBefore.longValue() + 1);
  }

  @Test
  public void shouldGetListOfBooks() {
    manager.addBook("List-Book_1", 5);
    manager.addBook("List-Book_2", 10);
    manager.addBook("List-Book_3", 15);

    Collection<Map<String, Object>> all = manager.searchBooks("List-Book");

    assertThat(all)
        .hasSize(3)
        .contains(book("List-Book_1", 5), book("List-Book_2", 10), book("List-Book_3", 15));
  }

  @Test
  public void shouldRollbackTransaction() {
    Number countBefore = manager.countBooks();

    manager.addBook("Test book TX", 1);

    Number countAfter = manager.countBooks();

    assertThat(countAfter).isEqualTo(countBefore.longValue() + 1);

    manager.rollback();

    Number countRollback = manager.countBooks();

    assertThat(countRollback).isEqualTo(countBefore.longValue());
  }

  @Test
  public void shouldCommitTransaction() {

    Number countBefore = manager.countBooks();

    manager.addBook("Test book TX", 1);

    Number countAfter = manager.countBooks();

    assertThat(countAfter).isEqualTo(countBefore.longValue() + 1);

    manager.commit();

    manager.rollback(); // this action doesn't change anything

    Number countRollback = manager.countBooks();

    assertThat(countRollback).isEqualTo(countBefore.longValue() + 1);
  }

  private Map<String, Object> book(String t, int s) {
    Map<String, Object> b = new HashMap<>();
    b.put("title", t);
    b.put("score", s);
    return b;
  }
}
