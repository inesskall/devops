-- SQL скрипт для исправления структуры таблиц event и reservation
-- Выполните этот скрипт в PostgreSQL, если хотите сохранить существующие данные

-- Для таблицы event
-- Сначала удаляем старую колонку id (если она не SERIAL)
-- ВНИМАНИЕ: Это удалит все данные! Выполняйте только если таблица пустая или данные не важны
-- ALTER TABLE event DROP COLUMN id;

-- Создаем новую колонку id с SERIAL (AUTO_INCREMENT)
-- ALTER TABLE event ADD COLUMN id SERIAL PRIMARY KEY;

-- Или если хотите сохранить данные, используйте:
-- 1. Создать sequence
CREATE SEQUENCE IF NOT EXISTS event_id_seq;

-- 2. Установить значение sequence на максимальный ID + 1
SELECT setval('event_id_seq', COALESCE((SELECT MAX(id) FROM event), 0) + 1, false);

-- 3. Изменить колонку id для использования sequence
ALTER TABLE event ALTER COLUMN id SET DEFAULT nextval('event_id_seq');
ALTER TABLE event ALTER COLUMN id SET NOT NULL;

-- То же самое для таблицы reservation
CREATE SEQUENCE IF NOT EXISTS reservation_id_seq;
SELECT setval('reservation_id_seq', COALESCE((SELECT MAX(id) FROM reservation), 0) + 1, false);
ALTER TABLE reservation ALTER COLUMN id SET DEFAULT nextval('reservation_id_seq');
ALTER TABLE reservation ALTER COLUMN id SET NOT NULL;
