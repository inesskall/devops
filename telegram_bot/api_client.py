"""
Клиент для работы с API Event Booking
"""
import requests
from typing import List, Dict, Optional
from config import API_BASE_URL, API_TIMEOUT
import logging

logger = logging.getLogger(__name__)


class APIClient:
    """Класс для взаимодействия с REST API"""
    
    def __init__(self, base_url: str = API_BASE_URL):
        self.base_url = base_url
        self.session = requests.Session()
        self.session.headers.update({
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        })
    
    def _make_request(self, method: str, endpoint: str, **kwargs) -> Optional[Dict]:
        """Выполняет HTTP запрос к API"""
        url = f"{self.base_url}{endpoint}"
        try:
            response = self.session.request(
                method=method,
                url=url,
                timeout=API_TIMEOUT,
                **kwargs
            )
            response.raise_for_status()
            return response.json() if response.content else None
        except requests.exceptions.RequestException as e:
            logger.error(f"API request failed: {e}")
            raise
    
    def get_events(self) -> List[Dict]:
        """Получить список всех событий"""
        return self._make_request('GET', '/events') or []
    
    def get_event(self, event_id: int) -> Dict:
        """Получить событие по ID"""
        return self._make_request('GET', f'/event/{event_id}')
    
    def get_available_events(self, date_from: str, date_to: str) -> List[Dict]:
        """Получить доступные события в диапазоне дат"""
        params = {'dateFrom': date_from, 'dateTo': date_to}
        return self._make_request('GET', '/events/availabilitySearch', params=params) or []
    
    def create_reservation(self, event_id: int, student_id: str, password: str) -> Dict:
        """
        Создать бронирование
        
        Примечание: API использует сессии, поэтому для создания бронирования
        нужно сначала авторизоваться, а затем использовать сессию.
        Для упрощения, можно модифицировать API чтобы принимать userId напрямую.
        """
        # Сначала логинимся
        login_data = {
            'studentId': student_id,
            'password': password
        }
        
        # Создаем новую сессию для логина
        login_session = requests.Session()
        login_response = login_session.post(
            f"{self.base_url}/login",
            json=login_data,
            timeout=API_TIMEOUT
        )
        
        if login_response.status_code != 200:
            try:
                error_data = login_response.json()
                error_message = error_data.get('message', 'Неизвестная ошибка')
            except:
                error_message = login_response.text or "Ошибка авторизации"
            raise Exception(f"Ошибка авторизации: {error_message}. Проверьте studentId и пароль.")
        
        # Получаем userId из ответа
        user_data = login_response.json()
        user_id = user_data.get('id')
        
        if not user_id:
            raise Exception("Не удалось получить ID пользователя после авторизации.")
        
        # Создаем бронирование используя ту же сессию
        reservation_data = {
            'eventId': event_id,
            'status': True
        }
        
        reservation_response = login_session.post(
            f"{self.base_url}/reservation",
            json=reservation_data,
            timeout=API_TIMEOUT
        )
        
        if reservation_response.status_code != 200:
            try:
                error_data = reservation_response.json()
                error_message = error_data.get('message', 'Неизвестная ошибка')
            except:
                error_message = reservation_response.text or "Ошибка при создании бронирования"
            raise Exception(f"Ошибка при создании бронирования: {error_message}")
        
        return reservation_response.json()
    
    def get_reservations(self) -> List[Dict]:
        """Получить список всех бронирований"""
        return self._make_request('GET', '/reservations') or []
    
    def get_reservation(self, reservation_id: int) -> Dict:
        """Получить бронирование по ID"""
        return self._make_request('GET', f'/reservation/{reservation_id}')
    
    def login(self, student_id: str, password: str) -> Dict:
        """Вход в систему"""
        login_data = {
            'studentId': student_id,
            'password': password
        }
        
        response = requests.post(
            f"{self.base_url}/login",
            json=login_data,
            timeout=API_TIMEOUT
        )
        
        if response.status_code != 200:
            try:
                error_data = response.json()
                error_message = error_data.get('message', 'Неизвестная ошибка')
            except:
                error_message = response.text or "Ошибка авторизации"
            raise Exception(f"Ошибка авторизации: {error_message}")
        
        return response.json()
    
    def register(self, student_id: str, name: str, surname: str, password: str) -> Dict:
        """Регистрация нового пользователя"""
        register_data = {
            'studentId': student_id,
            'name': name,
            'surname': surname,
            'password': password
        }
        
        response = requests.post(
            f"{self.base_url}/register",
            json=register_data,
            timeout=API_TIMEOUT
        )
        
        if response.status_code != 200:
            try:
                error_data = response.json()
                error_message = error_data.get('message', 'Неизвестная ошибка')
            except:
                error_message = response.text or "Ошибка регистрации"
            raise Exception(f"Ошибка регистрации: {error_message}")
        
        return response.json()

