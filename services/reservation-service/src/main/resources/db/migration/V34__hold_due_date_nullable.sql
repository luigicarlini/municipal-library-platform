-- src/main/resources/db/migration/V34__hold_due_date_nullable.sql
ALTER TABLE holds ALTER COLUMN due_date DROP NOT NULL;