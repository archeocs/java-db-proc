package org.procj.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
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

    manager.addBook("Test book");

    Number countAfter = manager.countBooks();

    assertThat(countAfter).isEqualTo(countBefore.longValue() + 1);
  }

  @Test
  public void shouldGetListOfBooks() {
    manager.addBook("B1");
    manager.addBook("B2");
    manager.addBook("B3");

    Collection<Map<String, Object>> all = manager.getTheBest();
    
    assertThat(all).hasSize(3);
    
  }

  @Test
  public void shouldRollbackTransaction() {
    Number countBefore = manager.countBooks();

    manager.addBook("Test book TX");

    Number countAfter = manager.countBooks();

    assertThat(countAfter).isEqualTo(countBefore.longValue() + 1);

    manager.rollback();

    Number countRollback = manager.countBooks();

    assertThat(countRollback).isEqualTo(countBefore.longValue());
  }

  @Test
  public void shouldCommitTransaction() {

    Number countBefore = manager.countBooks();

    manager.addBook("Test book TX");

    Number countAfter = manager.countBooks();

    assertThat(countAfter).isEqualTo(countBefore.longValue() + 1);

    manager.commit();

    manager.rollback(); // this action doesn't change anything

    Number countRollback = manager.countBooks();

    assertThat(countRollback).isEqualTo(countBefore.longValue() + 1);
  }
}
