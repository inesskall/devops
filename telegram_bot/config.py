"""
Конфигурационный файл для Telegram бота
"""
import os
from dotenv import load_dotenv

load_dotenv()

# Telegram Bot Token (получить у @BotFather)
TELEGRAM_BOT_TOKEN = os.getenv("TELEGRAM_BOT_TOKEN", "")

# API базовый URL
API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8080/api/v1")

# Настройки для работы с API
API_TIMEOUT = 10  # секунды

