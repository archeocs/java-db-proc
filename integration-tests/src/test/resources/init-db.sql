CREATE TABLE books (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100)
);

CREATE PROCEDURE count_books ()
BEGIN
  SELECT COUNT(*) FROM books;
END;

CREATE PROCEDURE add_book (title VARCHAR(100))
BEGIN
  INSERT INTO books(title) VALUES (title);
END;


CREATE PROCEDURE get_the_best () 
BEGIN
	SELECT * FROM books order by id desc;
END; 