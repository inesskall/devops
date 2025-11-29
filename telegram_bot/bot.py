"""
Telegram бот для бронирования событий
"""
import logging
from telegram import Update, InlineKeyboardButton, InlineKeyboardMarkup, BotCommand, ReplyKeyboardMarkup, KeyboardButton
from telegram.ext import (
    Application,
    CommandHandler,
    CallbackQueryHandler,
    MessageHandler,
    ConversationHandler,
    filters,
    ContextTypes
)
from api_client import APIClient
from config import TELEGRAM_BOT_TOKEN

# Настройка логирования
logging.basicConfig(
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    level=logging.INFO
)
logger = logging.getLogger(__name__)

# Состояния для ConversationHandler
# Регистрация
REG_NAME, REG_SURNAME, REG_STUDENT_ID, REG_PASSWORD = range(4)
# Логин
LOGIN_STUDENT_ID, LOGIN_PASSWORD = range(4, 6)
# Бронирование
WAITING_FOR_EVENT_ID, WAITING_FOR_BOOKING_PASSWORD = range(6, 8)

# Инициализация API клиента
api_client = APIClient()


def format_event(event: dict) -> str:
    """Форматирует событие для отображения в Telegram"""
    event_type = event.get('type', 'N/A')
    description = event.get('description', 'Нет описания')
    available_from = event.get('availableFrom', 'N/A')
    available_to = event.get('availableTo', 'N/A')
    status = '✅ Активно' if event.get('status') else '❌ Неактивно'
    
    text = f"🎯 *{event.get('name', 'Без названия')}*\n\n"
    text += f"📋 Тип: {event_type}\n"
    text += f"📝 Описание: {description}\n"
    text += f"📅 Доступно с: {available_from}\n"
    text += f"📅 Доступно до: {available_to}\n"
    text += f"🔔 Статус: {status}\n"
    text += f"🆔 ID: {event.get('id')}"
    
    return text


def is_authenticated(context: ContextTypes.DEFAULT_TYPE) -> bool:
    """Проверяет, авторизован ли пользователь"""
    return context.user_data.get('user_id') is not None


async def start(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Обработчик команды /start"""
    if is_authenticated(context):
        user = context.user_data.get('user')
        welcome_text = (
            f"👋 Привет, {user.get('name', 'пользователь')}!\n\n"
            "Вы уже авторизованы. Используйте команды из меню для работы с ботом.\n\n"
            "Доступные команды:\n"
            "/events - Показать все события\n"
            "/book - Забронировать событие\n"
            "/my_reservations - Мои бронирования\n"
            "/logout - Выйти из аккаунта\n"
            "/help - Показать справку"
        )
    else:
        welcome_text = (
            "👋 Добро пожаловать в бот для бронирования событий!\n\n"
            "Для начала работы необходимо авторизоваться:\n\n"
            "🔹 /register - Регистрация нового аккаунта\n"
            "🔹 /login - Вход в существующий аккаунт"
        )
        # Используем ReplyKeyboardMarkup для кнопок, которые отправляют команды в чат
        keyboard = [[
            KeyboardButton("📝 Регистрация"),
            KeyboardButton("🔐 Вход")
        ]]
        reply_markup = ReplyKeyboardMarkup(keyboard, resize_keyboard=True, one_time_keyboard=True)
        await update.message.reply_text(welcome_text, reply_markup=reply_markup)
        return
    
    await update.message.reply_text(welcome_text)


async def help_command(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Обработчик команды /help"""
    if not is_authenticated(context):
        help_text = (
            "📚 *Справка по использованию бота*\n\n"
            "Для начала работы необходимо авторизоваться:\n"
            "*/register* - Регистрация нового аккаунта\n"
            "*/login* - Вход в существующий аккаунт\n\n"
            "После авторизации будут доступны:\n"
            "*/events* - Показать список всех доступных событий\n"
            "*/book* - Забронировать событие\n"
            "*/my_reservations* - Показать мои бронирования"
        )
    else:
        help_text = (
            "📚 *Справка по использованию бота*\n\n"
            "*/events* - Показать список всех доступных событий\n"
            "*/book* - Забронировать событие\n"
            "*/event <id>* - Показать детали конкретного события\n"
            "*/my_reservations* - Показать мои бронирования\n"
            "*/logout* - Выйти из аккаунта"
        )
    await update.message.reply_text(help_text, parse_mode='Markdown')


# ========== РЕГИСТРАЦИЯ ==========
async def start_register(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Начать процесс регистрации"""
    if is_authenticated(context):
        if update.callback_query:
            await update.callback_query.answer("❌ Вы уже авторизованы. Используйте /logout для выхода.", show_alert=True)
        else:
            await update.message.reply_text("❌ Вы уже авторизованы. Используйте /logout для выхода.")
        return ConversationHandler.END
    
    # Убираем клавиатуру
    remove_keyboard = ReplyKeyboardMarkup([[]], resize_keyboard=True)
    
    if update.callback_query:
        await update.callback_query.answer()
        await update.callback_query.message.reply_text("📝 Регистрация нового пользователя\n\nВведите ваше имя:", reply_markup=remove_keyboard)
    else:
        await update.message.reply_text("📝 Регистрация нового пользователя\n\nВведите ваше имя:", reply_markup=remove_keyboard)
    return REG_NAME


async def receive_name(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Получить имя"""
    name = update.message.text.strip()
    context.user_data['reg_name'] = name
    await update.message.reply_text("Введите вашу фамилию:")
    return REG_SURNAME


async def receive_surname(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Получить фамилию"""
    surname = update.message.text.strip()
    context.user_data['reg_surname'] = surname
    await update.message.reply_text("Введите ваш Student ID:")
    return REG_STUDENT_ID


async def receive_reg_student_id(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Получить Student ID при регистрации"""
    student_id = update.message.text.strip()
    context.user_data['reg_student_id'] = student_id
    await update.message.reply_text("Введите пароль (минимум 4 символа):")
    return REG_PASSWORD


async def receive_reg_password(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Получить пароль и завершить регистрацию"""
    password = update.message.text
    name = context.user_data.get('reg_name')
    surname = context.user_data.get('reg_surname')
    student_id = context.user_data.get('reg_student_id')
    
    try:
        user_data = api_client.register(student_id, name, surname, password)
        
        # Сохраняем данные пользователя
        context.user_data['user_id'] = user_data.get('id')
        context.user_data['user'] = user_data
        context.user_data['student_id'] = student_id
        context.user_data['password'] = password  # Временно сохраняем для бронирований
        
        # Убираем клавиатуру после успешной регистрации
        remove_keyboard = ReplyKeyboardMarkup([[]], resize_keyboard=True)
        
        await update.message.reply_text(
            f"✅ Регистрация успешна!\n\n"
            f"👤 Имя: {name} {surname}\n"
            f"🆔 Student ID: {student_id}\n\n"
            "Теперь вы можете использовать все функции бота!",
            reply_markup=remove_keyboard
        )
        
        # Очищаем временные данные регистрации
        context.user_data.pop('reg_name', None)
        context.user_data.pop('reg_surname', None)
        context.user_data.pop('reg_student_id', None)
        
        return ConversationHandler.END
        
    except Exception as e:
        await update.message.reply_text(f"❌ Ошибка при регистрации: {str(e)}")
        return ConversationHandler.END


# ========== ЛОГИН ==========
async def start_login(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Начать процесс входа"""
    if is_authenticated(context):
        if update.callback_query:
            await update.callback_query.answer("❌ Вы уже авторизованы. Используйте /logout для выхода.", show_alert=True)
        else:
            await update.message.reply_text("❌ Вы уже авторизованы. Используйте /logout для выхода.")
        return ConversationHandler.END
    
    # Убираем клавиатуру
    remove_keyboard = ReplyKeyboardMarkup([[]], resize_keyboard=True)
    
    if update.callback_query:
        await update.callback_query.answer()
        await update.callback_query.message.reply_text("🔐 Вход в систему\n\nВведите ваш Student ID:", reply_markup=remove_keyboard)
    else:
        await update.message.reply_text("🔐 Вход в систему\n\nВведите ваш Student ID:", reply_markup=remove_keyboard)
    return LOGIN_STUDENT_ID


async def receive_login_student_id(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Получить Student ID при входе"""
    student_id = update.message.text.strip()
    context.user_data['login_student_id'] = student_id
    await update.message.reply_text("Введите ваш пароль:")
    return LOGIN_PASSWORD


async def receive_login_password(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Получить пароль и завершить вход"""
    password = update.message.text
    student_id = context.user_data.get('login_student_id')
    
    try:
        user_data = api_client.login(student_id, password)
        
        # Сохраняем данные пользователя
        context.user_data['user_id'] = user_data.get('id')
        context.user_data['user'] = user_data
        context.user_data['student_id'] = student_id
        context.user_data['password'] = password  # Временно сохраняем для бронирований
        
        name = user_data.get('name', '')
        surname = user_data.get('surname', '')
        
        # Убираем клавиатуру после успешного входа
        remove_keyboard = ReplyKeyboardMarkup([[]], resize_keyboard=True)
        
        await update.message.reply_text(
            f"✅ Вход выполнен успешно!\n\n"
            f"👤 Привет, {name} {surname}!\n\n"
            "Теперь вы можете использовать все функции бота!",
            reply_markup=remove_keyboard
        )
        
        # Очищаем временные данные логина
        context.user_data.pop('login_student_id', None)
        
        return ConversationHandler.END
        
    except Exception as e:
        await update.message.reply_text(f"❌ Ошибка при входе: {str(e)}")
        return ConversationHandler.END


async def logout(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Выход из аккаунта"""
    if not is_authenticated(context):
        await update.message.reply_text("❌ Вы не авторизованы.")
        return
    
    context.user_data.clear()
    await update.message.reply_text("✅ Вы успешно вышли из аккаунта.")


async def show_events(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Показать список всех событий"""
    if not is_authenticated(context):
        await update.message.reply_text("❌ Для просмотра событий необходимо авторизоваться.\nИспользуйте /register или /login")
        return
    
    try:
        events = api_client.get_events()
        
        if not events:
            await update.message.reply_text("📭 Событий пока нет.")
            return
        
        # Отправляем первые 10 событий (Telegram ограничение на длину сообщения)
        events_to_show = events[:10]
        
        text = f"📅 *Доступные события ({len(events)} всего):*\n\n"
        
        for event in events_to_show:
            text += f"🎯 *{event.get('name', 'Без названия')}*\n"
            text += f"   ID: {event.get('id')} | "
            text += f"{'✅ Активно' if event.get('status') else '❌ Неактивно'}\n\n"
        
        if len(events) > 10:
            text += f"\n_Показано 10 из {len(events)} событий. Используйте /event <id> для просмотра деталей._"
        
        # Создаем кнопки для каждого события
        keyboard = []
        for event in events_to_show:
            keyboard.append([
                InlineKeyboardButton(
                    f"{event.get('name', 'Событие')} (ID: {event.get('id')})",
                    callback_data=f"event_{event.get('id')}"
                )
            ])
        
        reply_markup = InlineKeyboardMarkup(keyboard)
        
        await update.message.reply_text(
            text,
            parse_mode='Markdown',
            reply_markup=reply_markup
        )
        
    except Exception as e:
        logger.error(f"Error showing events: {e}")
        await update.message.reply_text(f"❌ Ошибка при загрузке событий: {str(e)}")


async def show_event_details(message, event_id: int, context: ContextTypes.DEFAULT_TYPE = None):
    """Показать детали конкретного события"""
    if context and not is_authenticated(context):
        await message.reply_text("❌ Для просмотра событий необходимо авторизоваться.\nИспользуйте /register или /login")
        return
    
    try:
        event = api_client.get_event(event_id)
        
        if not event:
            await message.reply_text(f"❌ Событие с ID {event_id} не найдено.")
            return
        
        text = format_event(event)
        
        # Кнопка для бронирования (только если авторизован)
        keyboard = []
        if context and is_authenticated(context):
            keyboard = [[
                InlineKeyboardButton("📝 Забронировать", callback_data=f"book_{event_id}")
            ]]
        reply_markup = InlineKeyboardMarkup(keyboard) if keyboard else None
        
        await message.reply_text(
            text,
            parse_mode='Markdown',
            reply_markup=reply_markup
        )
        
    except Exception as e:
        logger.error(f"Error showing event details: {e}")
        await message.reply_text(f"❌ Ошибка: {str(e)}")


async def event_command(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Обработчик команды /event <id>"""
    if not is_authenticated(context):
        await update.message.reply_text("❌ Для просмотра событий необходимо авторизоваться.\nИспользуйте /register или /login")
        return
    
    try:
        event_id = int(context.args[0]) if context.args else None
        
        if not event_id:
            await update.message.reply_text("❌ Укажите ID события. Пример: /event 1")
            return
        
        await show_event_details(update.message, event_id, context)
        
    except ValueError:
        await update.message.reply_text("❌ Неверный формат ID. ID должен быть числом.")
    except Exception as e:
        logger.error(f"Error in event command: {e}")
        await update.message.reply_text(f"❌ Ошибка: {str(e)}")


async def button_callback(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Обработчик нажатий на inline кнопки (только для просмотра событий)"""
    query = update.callback_query
    await query.answer()
    
    data = query.data
    
    # Кнопки start_register и start_login обрабатываются через ConversationHandler
    if data.startswith("event_"):
        if not is_authenticated(context):
            await query.message.reply_text("❌ Для просмотра событий необходимо авторизоваться.\nИспользуйте /register или /login")
            return
        
        event_id = int(data.split("_")[1])
        try:
            event = api_client.get_event(event_id)
            text = format_event(event)
            keyboard = [[
                InlineKeyboardButton("📝 Забронировать", callback_data=f"book_{event_id}")
            ]]
            reply_markup = InlineKeyboardMarkup(keyboard)
            await query.message.edit_text(
                text,
                parse_mode='Markdown',
                reply_markup=reply_markup
            )
        except Exception as e:
            logger.error(f"Error in button_callback event_: {e}")
            await query.message.reply_text(f"❌ Ошибка: {str(e)}")


async def start_booking(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Начать процесс бронирования"""
    if not is_authenticated(context):
        await update.message.reply_text("❌ Для бронирования необходимо авторизоваться.\nИспользуйте /register или /login")
        return ConversationHandler.END
    
    await update.message.reply_text(
        "📝 Для бронирования события введите ID события:\n"
        "(Или используйте кнопку 'Забронировать' в деталях события)"
    )
    return WAITING_FOR_EVENT_ID


async def receive_event_id(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Получить ID события для бронирования"""
    try:
        event_id = int(update.message.text)
        context.user_data['booking_event_id'] = event_id
        
        # Проверяем, что событие существует
        event = api_client.get_event(event_id)
        if not event:
            await update.message.reply_text(f"❌ Событие с ID {event_id} не найдено.")
            return ConversationHandler.END
        
        # Используем сохраненные данные пользователя
        student_id = context.user_data.get('student_id')
        password = context.user_data.get('password')
        
        if not student_id or not password:
            await update.message.reply_text(
                f"✅ Событие найдено: {event.get('name')}\n\n"
                "Введите ваш пароль для подтверждения бронирования:"
            )
            return WAITING_FOR_BOOKING_PASSWORD
        
        # Сразу создаем бронирование
        try:
            result = api_client.create_reservation(event_id, student_id, password)
            reservation_id = result.get('id')
            await update.message.reply_text(
                f"✅ Бронирование успешно создано!\n\n"
                f"🎯 Событие: {event.get('name')}\n"
                f"🆔 ID бронирования: {reservation_id}"
            )
            context.user_data.pop('booking_event_id', None)
            return ConversationHandler.END
        except Exception as e:
            await update.message.reply_text(f"❌ Ошибка при создании бронирования: {str(e)}")
            return ConversationHandler.END
        
    except ValueError:
        await update.message.reply_text("❌ Неверный формат. Введите числовой ID события.")
        return WAITING_FOR_EVENT_ID
    except Exception as e:
        await update.message.reply_text(f"❌ Ошибка: {str(e)}")
        return ConversationHandler.END


async def receive_booking_password(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Получить пароль и создать бронирование"""
    password = update.message.text
    event_id = context.user_data.get('booking_event_id')
    student_id = context.user_data.get('student_id')
    
    try:
        result = api_client.create_reservation(event_id, student_id, password)
        
        event = api_client.get_event(event_id)
        reservation_id = result.get('id')
        await update.message.reply_text(
            f"✅ Бронирование успешно создано!\n\n"
            f"🎯 Событие: {event.get('name') if event else 'N/A'}\n"
            f"🆔 ID бронирования: {reservation_id}"
        )
        
        # Обновляем пароль в контексте
        context.user_data['password'] = password
        
        # Очищаем данные
        context.user_data.pop('booking_event_id', None)
        
        return ConversationHandler.END
        
    except Exception as e:
        await update.message.reply_text(f"❌ Ошибка при создании бронирования: {str(e)}")
        return ConversationHandler.END


async def my_reservations(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Показать мои бронирования"""
    if not is_authenticated(context):
        await update.message.reply_text("❌ Для просмотра бронирований необходимо авторизоваться.")
        return
    
    try:
        student_id = context.user_data.get('student_id')
        all_reservations = api_client.get_reservations()
        
        # Фильтруем бронирования текущего пользователя
        my_reservations = [
            r for r in all_reservations 
            if r.get('checkIn') == student_id
        ]
        
        if not my_reservations:
            await update.message.reply_text("📭 У вас пока нет бронирований.")
            return
        
        text = f"📋 *Ваши бронирования ({len(my_reservations)}):*\n\n"
        
        for res in my_reservations[:10]:  # Показываем первые 10
            event_id = res.get('eventId')
            try:
                event = api_client.get_event(event_id)
                event_name = event.get('name', 'Неизвестное событие') if event else 'Неизвестное событие'
            except:
                event_name = f"Событие #{event_id}"
            
            status = '✅ Активно' if res.get('status') else '❌ Отменено'
            text += f"🎯 {event_name}\n"
            text += f"   ID бронирования: {res.get('id')} | {status}\n\n"
        
        if len(my_reservations) > 10:
            text += f"\n_Показано 10 из {len(my_reservations)} бронирований._"
        
        await update.message.reply_text(text, parse_mode='Markdown')
        
    except Exception as e:
        logger.error(f"Error showing reservations: {e}")
        await update.message.reply_text(f"❌ Ошибка при загрузке бронирований: {str(e)}")


async def cancel(update: Update, context: ContextTypes.DEFAULT_TYPE):
    """Отменить текущую операцию"""
    # Сохраняем данные авторизации
    user_id = context.user_data.get('user_id')
    user = context.user_data.get('user')
    student_id = context.user_data.get('student_id')
    password = context.user_data.get('password')
    
    # Очищаем все данные
    context.user_data.clear()
    
    # Восстанавливаем данные авторизации
    if user_id:
        context.user_data['user_id'] = user_id
        context.user_data['user'] = user
        context.user_data['student_id'] = student_id
        context.user_data['password'] = password
    
    if update.callback_query:
        await update.callback_query.message.reply_text("❌ Операция отменена.")
    else:
        await update.message.reply_text("❌ Операция отменена.")
    return ConversationHandler.END


def main():
    """Запуск бота"""
    if not TELEGRAM_BOT_TOKEN:
        logger.error("TELEGRAM_BOT_TOKEN не установлен! Установите его в переменных окружения или .env файле.")
        return
    
    # Создаем приложение
    application = Application.builder().token(TELEGRAM_BOT_TOKEN).build()
    
    # Настраиваем меню команд
    async def post_init(app: Application):
        """Настройка меню команд после инициализации"""
        commands = [
            BotCommand("start", "Начать работу с ботом"),
            BotCommand("register", "Регистрация нового аккаунта"),
            BotCommand("login", "Вход в аккаунт"),
            BotCommand("events", "Показать все события"),
            BotCommand("book", "Забронировать событие"),
            BotCommand("my_reservations", "Мои бронирования"),
            BotCommand("logout", "Выйти из аккаунта"),
            BotCommand("help", "Справка по использованию"),
        ]
        await app.bot.set_my_commands(commands)
        logger.info("Меню команд настроено")
    
    application.post_init = post_init
    
    # Обработчик для текстовых кнопок регистрации и входа
    async def handle_button_text(update: Update, context: ContextTypes.DEFAULT_TYPE):
        """Обработчик текстовых кнопок"""
        text = update.message.text.strip()
        if text == "📝 Регистрация":
            return await start_register(update, context)
        elif text == "🔐 Вход":
            return await start_login(update, context)
        return None
    
    # Обработчик для регистрации
    register_handler = ConversationHandler(
        entry_points=[
            CommandHandler('register', start_register),
            MessageHandler(filters.TEXT & filters.Regex("^📝 Регистрация$"), handle_button_text)
        ],
        states={
            REG_NAME: [MessageHandler(filters.TEXT & ~filters.COMMAND, receive_name)],
            REG_SURNAME: [MessageHandler(filters.TEXT & ~filters.COMMAND, receive_surname)],
            REG_STUDENT_ID: [MessageHandler(filters.TEXT & ~filters.COMMAND, receive_reg_student_id)],
            REG_PASSWORD: [MessageHandler(filters.TEXT & ~filters.COMMAND, receive_reg_password)],
        },
        fallbacks=[CommandHandler('cancel', cancel)],
    )
    
    # Обработчик для логина
    login_handler = ConversationHandler(
        entry_points=[
            CommandHandler('login', start_login),
            MessageHandler(filters.TEXT & filters.Regex("^🔐 Вход$"), handle_button_text)
        ],
        states={
            LOGIN_STUDENT_ID: [MessageHandler(filters.TEXT & ~filters.COMMAND, receive_login_student_id)],
            LOGIN_PASSWORD: [MessageHandler(filters.TEXT & ~filters.COMMAND, receive_login_password)],
        },
        fallbacks=[CommandHandler('cancel', cancel)],
    )
    
    # Обработчик для бронирования через команду
    booking_handler = ConversationHandler(
        entry_points=[
            CommandHandler('book', start_booking),
        ],
        states={
            WAITING_FOR_EVENT_ID: [MessageHandler(filters.TEXT & ~filters.COMMAND, receive_event_id)],
            WAITING_FOR_BOOKING_PASSWORD: [MessageHandler(filters.TEXT & ~filters.COMMAND, receive_booking_password)],
        },
        fallbacks=[CommandHandler('cancel', cancel)],
    )
    
    # Обработчик для кнопки 'Забронировать'
    async def handle_book_button(update: Update, context: ContextTypes.DEFAULT_TYPE):
        """Обработчик кнопки 'Забронировать'"""
        if not is_authenticated(context):
            await update.callback_query.answer("❌ Необходимо авторизоваться", show_alert=True)
            return ConversationHandler.END
        
        query = update.callback_query
        await query.answer()
        
        event_id = int(query.data.split("_")[1])
        context.user_data['booking_event_id'] = event_id
        
        try:
            event = api_client.get_event(event_id)
            if not event:
                await query.message.reply_text(f"❌ Событие с ID {event_id} не найдено.")
                return ConversationHandler.END
            
            # Используем сохраненные данные пользователя
            student_id = context.user_data.get('student_id')
            password = context.user_data.get('password')
            
            if not student_id or not password:
                await query.message.reply_text(
                    f"✅ Событие: {event.get('name')}\n\n"
                    "Введите ваш пароль для подтверждения бронирования:"
                )
                return WAITING_FOR_BOOKING_PASSWORD
            
            # Сразу создаем бронирование
            try:
                result = api_client.create_reservation(event_id, student_id, password)
                reservation_id = result.get('id')
                await query.message.reply_text(
                    f"✅ Бронирование успешно создано!\n\n"
                    f"🎯 Событие: {event.get('name')}\n"
                    f"🆔 ID бронирования: {reservation_id}"
                )
                context.user_data.pop('booking_event_id', None)
                return ConversationHandler.END
            except Exception as e:
                await query.message.reply_text(f"❌ Ошибка при создании бронирования: {str(e)}")
                return ConversationHandler.END
                
        except Exception as e:
            logger.error(f"Error in handle_book_button: {e}")
            await query.message.reply_text(f"❌ Ошибка: {str(e)}")
            return ConversationHandler.END
    
    # Создаем отдельный ConversationHandler для кнопки бронирования
    booking_from_button_handler = ConversationHandler(
        entry_points=[
            CallbackQueryHandler(handle_book_button, pattern="^book_")
        ],
        states={
            WAITING_FOR_BOOKING_PASSWORD: [MessageHandler(filters.TEXT & ~filters.COMMAND, receive_booking_password)],
        },
        fallbacks=[CommandHandler('cancel', cancel)],
    )
    
    # Регистрируем обработчики (важен порядок!)
    application.add_handler(register_handler)
    application.add_handler(login_handler)
    application.add_handler(booking_handler)
    application.add_handler(booking_from_button_handler)
    application.add_handler(CommandHandler("start", start))
    application.add_handler(CommandHandler("help", help_command))
    application.add_handler(CommandHandler("events", show_events))
    application.add_handler(CommandHandler("event", event_command))
    application.add_handler(CommandHandler("my_reservations", my_reservations))
    application.add_handler(CommandHandler("logout", logout))
    application.add_handler(CallbackQueryHandler(button_callback))
    
    # Запускаем бота
    logger.info("Бот запущен...")
    application.run_polling(allowed_updates=Update.ALL_TYPES)


if __name__ == '__main__':
    main()

