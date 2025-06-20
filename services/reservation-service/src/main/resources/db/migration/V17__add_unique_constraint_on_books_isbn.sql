ALTER TABLE books
  ADD CONSTRAINT uq_books_isbn UNIQUE (isbn);