CREATE TABLE books (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100),
  score INTEGER
);

CREATE PROCEDURE count_books ()
BEGIN
  SELECT COUNT(*) FROM books;
END;

CREATE PROCEDURE add_book (title VARCHAR(100), score INTEGER)
BEGIN
  INSERT INTO books(title, score) VALUES (title, score);
END;


CREATE PROCEDURE search_books (prefix VARCHAR(100)) 
BEGIN
	SELECT title, score FROM books WHERE title like concat(prefix,'%') order by id desc;
END; 