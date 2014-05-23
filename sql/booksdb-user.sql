drop user 'books'@'localhost';
create user 'books'@'localhost' identified by 'books';
grant all privileges on booksdb.* to 'books'@'localhost';
flush privileges;