package org.procj.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.procj.core.Procj;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public class ProcjIT {

  public static final DockerImageName MYSQL_IMAGE = DockerImageName.parse("mysql:5.7.22");
  private static MySQLContainer<?> MYSQL;

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

  @Test
  public void shouldAddBookToDatabase() {
    Properties config = new Properties();
    config.setProperty("jdbc.driver", MYSQL.getDriverClassName());
    config.setProperty("url", MYSQL.getJdbcUrl());
    config.setProperty("user", MYSQL.getUsername());
    config.setProperty("password", MYSQL.getPassword());
    config.setProperty("useSSL", "false");
    BooksManager manager = Procj.getInstance().create(BooksManager.class, "jdbc", config);

    Number countBefore = manager.countBooks();

    manager.addBook("Test book");

    Number countAfter = manager.countBooks();

    assertThat(countAfter).isEqualTo(countBefore.longValue() + 1);
  }
}
