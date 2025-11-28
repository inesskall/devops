-- SQL скрипт для создания enum типа и обновления таблицы event
-- Выполните этот скрипт в PostgreSQL

-- 1. Создаем enum тип, если он не существует
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'valid_types_of_hotels_enum') THEN
        CREATE TYPE valid_types_of_hotels_enum AS ENUM ('CONCERT', 'WORKSHOP', 'CONFERENCE');
    END IF;
END $$;

-- 2. Проверяем текущий тип колонки type
-- Выполните это, чтобы увидеть текущий тип:
-- SELECT column_name, data_type, udt_name 
-- FROM information_schema.columns 
-- WHERE table_name = 'event' AND column_name = 'type';

-- 3. Если колонка type не является enum, изменяем её тип
-- ВНИМАНИЕ: Это может занять время, если в таблице много данных
-- Если колонка уже имеет правильный тип, эта команда не изменит ничего
DO $$ 
BEGIN
    -- Проверяем, является ли колонка уже enum типом
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'event' 
        AND column_name = 'type' 
        AND udt_name != 'valid_types_of_hotels_enum'
    ) THEN
        -- Если колонка имеет другой тип (например, VARCHAR), конвертируем её
        -- Сначала конвертируем существующие значения в текст, затем в enum
        ALTER TABLE event 
        ALTER COLUMN type TYPE valid_types_of_hotels_enum 
        USING type::text::valid_types_of_hotels_enum;
    END IF;
END $$;

-- 4. Проверяем результат
SELECT column_name, data_type, udt_name 
FROM information_schema.columns 
WHERE table_name = 'event' AND column_name = 'type';

