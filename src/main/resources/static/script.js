// API BASE URL
const API_BASE_URL = 'http://localhost:8080/api/v1';

// Глобальная переменная для хранения событий
let events = [];

// ФУНКЦИИ ДЛЯ РАБОТЫ С API
async function fetchEvents() {
    try {
        const response = await fetch(`${API_BASE_URL}/events`);
        if (!response.ok) {
            throw new Error('Ошибка при загрузке событий');
        }
        const data = await response.json();
        return data;
    } catch (error) {
        console.error('Ошибка при загрузке событий:', error);
        return [];
    }
}

async function fetchReservations() {
    try {
        const response = await fetch(`${API_BASE_URL}/reservations`);
        if (!response.ok) {
            throw new Error('Ошибка при загрузке бронирований');
        }
        const data = await response.json();
        return data;
    } catch (error) {
        console.error('Ошибка при загрузке бронирований:', error);
        return [];
    }
}

async function createReservation(reservationData) {
    try {
        const response = await fetch(`${API_BASE_URL}/reservation`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(reservationData)
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Ошибка при создании бронирования');
        }
        return await response.json();
    } catch (error) {
        console.error('Ошибка при создании бронирования:', error);
        throw error;
    }
}

// Преобразование данных события из API в формат для фронтенда
function transformEvent(event) {
    // Определяем категорию на основе типа события
    let category = 'conference';
    if (event.type === 'WORKSHOP') {
        category = 'workshop';
    } else if (event.type === 'CONCERT') {
        category = 'festival';
    } else if (event.type === 'CONFERENCE') {
        category = 'conference';
    }

    // Форматируем дату
    // availableFrom содержит только дату в формате "YYYY-MM-DD" без времени
    // Парсим дату правильно, чтобы избежать проблем с часовыми поясами
    let dateFrom;
    if (event.availableFrom) {
        // Парсим дату в формате YYYY-MM-DD и устанавливаем время по умолчанию (10:00)
        const [year, month, day] = event.availableFrom.split('-').map(Number);
        dateFrom = new Date(year, month - 1, day, 10, 0, 0); // 10:00 по умолчанию
    } else {
        // Если даты нет, используем текущую дату с временем 10:00
        const now = new Date();
        dateFrom = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 10, 0, 0);
    }
    const dateStr = formatDate(dateFrom);
    const timeStr = formatTime(dateFrom);

    // Получаем изображение по ID события
    // Формат: event-{id}-img.jpg (или .png, .jpeg)
    // Картинки соответствуют ID события напрямую
    const imageUrl = `/img/event-${event.id}-img.jpg`;

    return {
        id: event.id,
        title: event.name,
        category: category,
        date: dateStr,
        time: timeStr,
        location: 'SDU Campus', // Можно добавить поле location в Event если нужно
        description: event.description,
        price: 'Бесплатно', // Можно добавить поле price в Event если нужно
        image: imageUrl,
        booked: 0, // Будет обновлено после загрузки бронирований
        total: 100, // Можно добавить поле total в Event если нужно
        status: event.status,
        availableFrom: event.availableFrom,
        availableTo: event.availableTo
    };
}

// Форматирование даты
function formatDate(date) {
    const months = ['января', 'февраля', 'марта', 'апреля', 'мая', 'июня',
                   'июля', 'августа', 'сентября', 'октября', 'ноября', 'декабря'];
    const day = date.getDate();
    const month = months[date.getMonth()];
    return `${day} ${month}`;
}

// Форматирование времени
function formatTime(date) {
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${hours}:${minutes}`;
}

// Обработка ошибок загрузки изображения
function handleImageError(img, eventId) {
    // Пробуем разные расширения файлов
    // Используем eventId напрямую для формирования пути к изображению
    const extensions = ['jpg', 'png', 'jpeg'];
    const currentSrc = img.src;
    const basePath = `/img/event-${eventId}-img.`;
    
    // Определяем текущее расширение
    const currentExt = currentSrc.split('.').pop().split('?')[0];
    const currentIndex = extensions.indexOf(currentExt);
    
    if (currentIndex >= 0 && currentIndex < extensions.length - 1) {
        // Пробуем следующее расширение
        img.src = basePath + extensions[currentIndex + 1];
    } else {
        // Если все расширения перепробованы, используем заглушку
        img.src = '/img/event-default.jpg';
        img.onerror = null; // Предотвращаем бесконечный цикл
    }
}

// Подсчет забронированных мест для события
function countBookingsForEvent(eventId, reservations) {
    return reservations.filter(r => r.eventId === eventId).length;
}

// ИНИЦИАЛИЗАЦИЯ ДАННЫХ
async function initializeData() {
    try {
        // Загружаем события и бронирования параллельно
        const [eventsData, reservationsData] = await Promise.all([
            fetchEvents(),
            fetchReservations()
        ]);

        // Преобразуем события
        events = eventsData.map(transformEvent);

        // Подсчитываем бронирования для каждого события
        events.forEach(event => {
            event.booked = countBookingsForEvent(event.id, reservationsData);
        });

        // Рендерим события
        renderEvents();
    } catch (error) {
        console.error('Ошибка при инициализации данных:', error);
        const grid = document.getElementById("eventsGrid");
        if (grid) {
            grid.innerHTML = "<p style='text-align:center; color:#666;'>Ошибка при загрузке событий. Пожалуйста, обновите страницу.</p>";
        }
    }
}

// ПОКАЗ СТРАНИЦ
function showPage(pageId) {
    const pages = document.querySelectorAll('.page');
    pages.forEach(p => p.classList.remove('active'));

    const page = document.getElementById(pageId);
    if (page) {
        page.classList.add('active');
        window.scrollTo(0, 0);
    }
}

const menuToggle = document.getElementById('menu-toggle');
const navLinks = document.getElementById('nav-links');
const links = document.querySelectorAll('#nav-links a');

if (menuToggle && navLinks) {
    menuToggle.addEventListener('click', () => {
        navLinks.classList.toggle('active');
    });
}

if (links) {
    links.forEach(link => {
        link.addEventListener('click', () => {
            navLinks.classList.remove('active');
        })
    });
}

// РЕНДЕР КАРТОЧЕК
function renderEvents(filteredEvents = events) {
    const grid = document.getElementById("eventsGrid");
    if (!grid) return;

    grid.innerHTML = "";

    if (filteredEvents.length === 0) {
        grid.innerHTML = "<p style='text-align:center; color:#666;'>Ничего не найдено</p>";
        return;
    }

    filteredEvents.forEach(ev => {
        // Показываем только активные события
        if (!ev.status) return;

        const isFull = ev.booked >= ev.total;
        const seatsClass = isFull ? 'seats-full' : '';
        grid.innerHTML += `
        <div class="event-card">
            <div class="event-image">
                <img src="${ev.image}" alt="${ev.title}" onerror="handleImageError(this, ${ev.id})">
            </div>
            <div class="event-content">
                <h3 class="event-title">${ev.title}</h3>
    
                <div class="event-meta">
                    <div class="event-meta-item"><img src="img/date-img.svg" alt="date" width="16" height="16"> ${ev.date}</div>
                    <div class="event-meta-item"><img src="img/alarm-img.svg" alt="time" width="16" height="16"> ${ev.time}</div>
                    <div class="event-meta-item"><img src="img/location-black-img.svg" alt="location" width="16" height="16"> ${ev.location}</div>
                </div>

                <p class="event-description">${ev.description}</p>

                <div class="event-seats ${seatsClass}">
                    <span class="seats-label">Места:</span>
                    <span class="seats-count">${ev.booked}/${ev.total}</span>
                </div>

                <div class="event-footer">
                    <span class="event-price">${ev.price}</span>
                    <button class="btn btn-primary ${isFull ? 'btn-disabled' : ''}" onclick="openModal(${ev.id})" ${isFull ? 'disabled' : ''}>${isFull ? 'Мест нет' : 'Бронировать'}</button>
                </div>
            </div>
        </div>
        `;
    });
}

// ПОИСК
function searchEvents() {
    const text = document.getElementById("searchInput").value.toLowerCase();
    const category = document.getElementById("categorySelect").value;

    const filtered = events.filter(ev =>
        (ev.title.toLowerCase().includes(text)) &&
        (category === "" || ev.category === category) &&
        ev.status // Показываем только активные события
    );

    renderEvents(filtered);
}

// МОДАЛЬНОЕ ОКНО
let currentEventId = null;

function openModal(eventId) {
    const event = events.find(ev => ev.id === eventId);
    if (!event) return;

    currentEventId = eventId;
    const modalTitle = document.getElementById("modalEventTitle");
    if (modalTitle) {
        modalTitle.textContent = event.title;
    }
    const modal = document.getElementById("bookingModal");
    if (modal) {
        modal.style.display = "block";
    }
    
    // Очистка формы
    const form = document.getElementById("bookingForm");
    if (form) {
        form.reset();
    }
}

function closeModal() {
    const modal = document.getElementById("bookingModal");
    if (modal) {
        modal.style.display = "none";
    }
    currentEventId = null;
    const form = document.getElementById("bookingForm");
    if (form) {
        form.reset();
    }
}

// Закрытие модального окна при клике вне его области
const bookingModal = document.getElementById("bookingModal");
if (bookingModal) {
    bookingModal.addEventListener("click", function(e) {
        if (e.target.id === "bookingModal") {
            closeModal();
        }
    });
}

// БРОНИРОВАНИЕ
const bookingForm = document.getElementById("bookingForm");
if (bookingForm) {
    bookingForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        if (!currentEventId) {
            closeModal();
            return;
        }

        const event = events.find(ev => ev.id === currentEventId);
        if (!event) {
            closeModal();
            return;
        }

        // Проверка на наличие свободных мест
        if (event.booked >= event.total) {
            alert("К сожалению, все места заняты!");
            closeModal();
            return;
        }

        // Получение данных формы
        const bookingId = document.getElementById("bookingId").value.trim();
        const bookingName = document.getElementById("bookingName").value.trim();
        const bookingSurname = document.getElementById("bookingSurname").value.trim();

        // Валидация
        if (!bookingId || !bookingName || !bookingSurname) {
            alert("Пожалуйста, заполните все поля!");
            return;
        }

        try {
            // Создаем бронирование через API
            // checkIn используется для хранения ID студента (чтобы предотвратить двойную регистрацию)
            const reservationData = {
                eventId: currentEventId,
                checkIn: bookingId, // Сохраняем ID студента для проверки уникальности
                status: true
            };

            await createReservation(reservationData);

            // Обновляем данные
            await initializeData();

            // Обновление отображения событий с учетом фильтров
            searchEvents();

            closeModal();

            const msg = document.getElementById("successMessage");
            if (msg) {
                msg.style.display = "block";
                setTimeout(() => {
                    msg.style.display = "none";
                }, 3000);
            }
        } catch (error) {
            alert(error.message || "Ошибка при создании бронирования. Пожалуйста, попробуйте еще раз.");
        }
    });
}

// ОБРАБОТКА ФОРМЫ КОНТАКТОВ
const contactForm = document.getElementById("contactForm");
if (contactForm) {
    contactForm.addEventListener("submit", e => {
        e.preventDefault();
        alert("Ваше сообщение отправлено. Мы свяжемся с вами!");
        e.target.reset();
    });
}

// МОДАЛЬНОЕ ОКНО СОЗДАНИЯ СОБЫТИЯ
function openCreateEventModal() {
    const modal = document.getElementById("createEventModal");
    if (modal) {
        modal.style.display = "block";
    }
}

function closeCreateEventModal() {
    const modal = document.getElementById("createEventModal");
    if (modal) {
        modal.style.display = "none";
    }
}

// Закрытие модального окна создания события при клике вне его области
const createEventModal = document.getElementById("createEventModal");
if (createEventModal) {
    createEventModal.addEventListener("click", function(e) {
        if (e.target.id === "createEventModal") {
            closeCreateEventModal();
        }
    });
}

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', () => {
    initializeData();
});
