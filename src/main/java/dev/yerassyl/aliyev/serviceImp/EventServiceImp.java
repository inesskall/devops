package dev.yerassyl.aliyev.serviceImp;

import dev.yerassyl.aliyev.constants.ErrorMessages;
import dev.yerassyl.aliyev.dto.IdEntity;
import dev.yerassyl.aliyev.dto.SuccessEntity;
import dev.yerassyl.aliyev.entity.Event;
import dev.yerassyl.aliyev.entity.Reservation;
import dev.yerassyl.aliyev.exception.InvalidRequestException;
import dev.yerassyl.aliyev.repository.EventRepository;
import dev.yerassyl.aliyev.repository.ReservationRepository;
import dev.yerassyl.aliyev.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * Event Service that preforms operations regarding Event API Calls
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImp implements EventService {

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final JdbcTemplate jdbcTemplate;
    
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Return all existing Event objects in the database
     *
     * @return List<Event>
     */
    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    /**
     * Return existing Event with pagination
     *
     * @param pageNo
     * @param pageSize
     * @param sortBy
     * @return
     */
    @Override
    public List<Event> getEventPagedList(Integer pageNo, Integer pageSize, String sortBy) {

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.Direction.ASC, sortBy);
        Page<Event> pagedResult = eventRepository.findAll(paging);

        if (pagedResult.hasContent()) {
            return pagedResult.getContent();
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Returns a user specified Event item through the Event id
     *
     * @param id
     * @return Event
     */
    @Override
    public Event getEvent(Integer id) {
        validateEventExistenceById(id);
        return eventRepository.findById(id).get();

        // Ot without validateEventExistenceById(id);
        // Optional<Event> event = eventRepository.findById(id);
        // return event.isPresent() ? event.get() : null;
    }

    /**
     * Returns all Event objects in the database that are available in between user specified dates
     *
     * @param dateFrom
     * @param dateTo
     * @return
     */
    @Override
    public List<Event> getAvailable(String dateFrom, String dateTo) {
        return eventRepository.findAllBetweenDates(dateFrom, dateTo);
    }

    /**
     * Saves a user specified Event object to the database
     *
     * @param event
     * @return
     */
    @Override
    public IdEntity saveEvent(@Valid Event event) {
        //If dates are empty strings make them null values so that they can be accepted by the database
        if ((!StringUtils.hasText(event.getAvailableFrom())) && (!(StringUtils.hasText(event.getAvailableTo())))) {
            event.setAvailableFrom(null);
            event.setAvailableTo(null);
        }
        
        event = eventRepository.save(event);

        IdEntity idEntity = new IdEntity();
        idEntity.setId(event.getId());
        return idEntity;
    }

    /**
     * Deletes a user specified Event object from the database
     *
     * @param id
     * @return
     */
    @Override
    public SuccessEntity deleteEvent(Integer id) {
        validateEventExistenceById(id);
        
        log.info("Starting deletion of event with id = {}", id);
        
        // Удаляем все резервации, связанные с этим событием (каскадное удаление)
        int deletedReservations = jdbcTemplate.update("DELETE FROM reservation WHERE event_id = ?", id);
            log.info("Deleted {} reservations associated with event id = {}", deletedReservations, id);
        
        // Удаляем само событие используя нативный SQL
        int deletedCount = jdbcTemplate.update("DELETE FROM event WHERE id = ?", id);
        log.info("SQL DELETE executed for event id = {}, deletedCount = {}", id, deletedCount);
        
        if (deletedCount == 0) {
            log.error("No event found with id = {} to delete", id);
            throw new InvalidRequestException("Failed to delete event with id " + id + " - event not found");
        }
        
        // Очищаем кэш EntityManager, чтобы изменения были видны
        entityManager.clear();
        log.info("EntityManager cache cleared after deletion");
        
        // Принудительно синхронизируем изменения с базой данных
        entityManager.flush();
        
        // Синхронизируем sequence с максимальным ID в таблице
        try {
            updateEventSequenceToMaxId();
        } catch (Exception e) {
            // Логируем, но не прерываем транзакцию - обновление sequence не критично
            log.debug("Could not update event sequence: {}", e.getMessage());
        }
        
        SuccessEntity successEntity = new SuccessEntity();
        successEntity.setSuccess(true);
        return successEntity;
    }
    
    /**
     * Обновляет sequence для таблицы event до максимального ID + 1
     * Это гарантирует, что следующий ID будет корректным после удаления событий
     */
    private void updateEventSequenceToMaxId() {
        try {
            // Находим имя sequence для таблицы event
            String sequenceName = jdbcTemplate.queryForObject(
                "SELECT pg_get_serial_sequence('event', 'id')", 
                String.class
            );
            
            if (sequenceName != null && !sequenceName.isEmpty()) {
                // Удаляем схему из имени (оставляем только имя sequence)
                String seqName = sequenceName.contains(".") 
                    ? sequenceName.substring(sequenceName.lastIndexOf(".") + 1) 
                    : sequenceName;
                
                // Получаем максимальный ID из таблицы
                Integer maxId = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(MAX(id), 0) FROM event", 
                    Integer.class
                );
                
                if (maxId == null) {
                    maxId = 0;
                }
                
                // Устанавливаем sequence на максимальный ID + 1
                // Если таблица пустая (maxId = 0), то следующий ID будет 1
                int nextId = maxId + 1;
                jdbcTemplate.execute("SELECT setval('" + seqName + "', " + nextId + ", false)");
                log.info("Event sequence '{}' updated to {} (max ID was {})", seqName, nextId, maxId);
            }
        } catch (Exception e) {
            // Логируем, но не прерываем транзакцию
            log.debug("Could not update event sequence: {}", e.getMessage());
        }
    }
    
    /**
     * Сбрасывает sequence для таблицы event, чтобы следующий ID начинался с 1
     * Используется только когда таблица полностью пустая
     */
    private void resetEventSequence() {
        try {
            // Находим имя sequence для таблицы event
            String sequenceName = jdbcTemplate.queryForObject(
                "SELECT pg_get_serial_sequence('event', 'id')", 
                String.class
            );
            
            if (sequenceName != null && !sequenceName.isEmpty()) {
                // Удаляем схему из имени (оставляем только имя sequence)
                String seqName = sequenceName.contains(".") 
                    ? sequenceName.substring(sequenceName.lastIndexOf(".") + 1) 
                    : sequenceName;
                
                // Сбрасываем sequence на 1, чтобы следующий ID был 1
                // setval не может быть 0, минимальное значение - 1
                jdbcTemplate.execute("SELECT setval('" + seqName + "', 1, false)");
                log.info("Event sequence '{}' reset to start from 1 (table is now empty)", seqName);
            }
        } catch (Exception e) {
            // Логируем, но не прерываем транзакцию
            log.debug("Could not reset event sequence: {}", e.getMessage());
        }
    }

    /**
     * Updates a pre-existing Event object in the database
     *
     * @param event
     * @return
     */
    @Override
    public SuccessEntity patchEvent(Event event) {
        validateEventExistenceById(event.getId());
        doesReservationOverlap(event);
        SuccessEntity successEntity = new SuccessEntity();
        event = eventRepository.save(event);
        successEntity.setSuccess(eventRepository.existsById(event.getId()));
        return successEntity;
    }

    /**
     * Checks to see if a reservation date overlaps with the inventory dates
     *
     * @param event
     */
    @Override
    public void doesReservationOverlap(Event event) {
        // Упрощенная версия - так как checkIn/checkOut больше не используются,
        // просто проверяем, есть ли бронирования для этого события
        Integer eventId = event.getId();
        List<Reservation> matchingReservationList = reservationRepository.findAll().stream()
                .filter(reservations -> reservations.getEventId().equals(eventId))
                .toList();

        if (matchingReservationList.size() != 0) {
            throw new InvalidRequestException(ErrorMessages.INVALID_EVENT_UPDATE);
        }
    }

    /**
     * Checks the existence of a Event object in the database
     *
     * @param id
     * @return
     */
    @Override
    public boolean validateEventExistenceById(Integer id) {
        if (!eventRepository.existsById(id)) {
            log.error("Invalid ID: The entered id = {} does not exist.", id);
            throw new InvalidRequestException(ErrorMessages.INVALID_ID_EXISTENCE);
        } else {
            return true;
        }
    }
    
}
