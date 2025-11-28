-- SQL скрипт для удаления колонок check_in, check_out и guests из таблицы reservation
-- Выполните этот скрипт в PostgreSQL

-- Сначала делаем колонки nullable (если они еще не nullable)
ALTER TABLE reservation ALTER COLUMN check_in DROP NOT NULL;
ALTER TABLE reservation ALTER COLUMN check_out DROP NOT NULL;
ALTER TABLE reservation ALTER COLUMN guests DROP NOT NULL;

-- Затем удаляем колонки (если хотите полностью их убрать)
-- ВНИМАНИЕ: Это удалит данные в этих колонках!
-- ALTER TABLE reservation DROP COLUMN IF EXISTS check_in;
-- ALTER TABLE reservation DROP COLUMN IF EXISTS check_out;
-- ALTER TABLE reservation DROP COLUMN IF EXISTS guests;

-- Или просто оставьте их как nullable - они не будут использоваться в коде

