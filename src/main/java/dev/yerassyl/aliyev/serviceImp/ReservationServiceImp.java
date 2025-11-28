package dev.yerassyl.aliyev.serviceImp;

import dev.yerassyl.aliyev.constants.ErrorMessages;
import dev.yerassyl.aliyev.dto.IdEntity;
import dev.yerassyl.aliyev.dto.SuccessEntity;
import dev.yerassyl.aliyev.entity.Event;
import dev.yerassyl.aliyev.entity.Reservation;
import dev.yerassyl.aliyev.exception.InvalidRequestException;
import dev.yerassyl.aliyev.repository.EventRepository;
import dev.yerassyl.aliyev.repository.ReservationRepository;
import dev.yerassyl.aliyev.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Reservation Service tha performs operations regarding Reservation API Calls
 */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class ReservationServiceImp implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;

    /**
     * Returns all existing Reservation objects in the database
     * @return
     */
    @Override
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    /**
     * Finds a user specified Reservation in the database
     * @param id
     * @return
     */
    @Override
    public Reservation getReservation(Integer id) {
        validateReservationExistence(id);
        return reservationRepository.findById(id).get();
    }

    /**
     * Saves a user created Reservation object to the database
     * @param reservations
     * @return
     */
    @Override
    public IdEntity saveReservation(Reservation reservations) {
        Integer reservationsInventoryId = reservations.getEventId();

        //boolean to determine if the Reservation is valid through the existence of the inventory ID.
        //if the inventory ID exists, then continue
        if (validateEventExistenceById(reservationsInventoryId)) {
            // Упрощенная логика - просто сохраняем бронирование
            // Проверяем только, что событие существует и активно
            Event event = eventRepository.getById(reservations.getEventId());
            if (!event.isStatus()) {
                throw new InvalidRequestException("Событие неактивно. Невозможно создать бронирование.");
            }
            
            // Проверяем, не превышен ли лимит мест (100)
            long currentReservations = reservationRepository.findAll().stream()
                    .filter(r -> r.getEventId().equals(reservations.getEventId()))
                    .count();
            if (currentReservations >= 100) {
                throw new InvalidRequestException("Все места заняты. Невозможно создать бронирование.");
            }
            
            // Проверяем, не зарегистрирован ли уже этот студент (ID) на это событие
            if (reservations.getCheckIn() != null && !reservations.getCheckIn().isEmpty()) {
                boolean alreadyRegistered = reservationRepository.findAll().stream()
                        .anyMatch(r -> r.getEventId().equals(reservations.getEventId()) 
                                && reservations.getCheckIn().equals(r.getCheckIn()));
                if (alreadyRegistered) {
                    throw new InvalidRequestException("Вы уже зарегистрированы на это событие. Один ID может зарегистрироваться только один раз на каждое событие.");
                }
            }
            
            Reservation savedReservation = reservationRepository.save(reservations);
            IdEntity idEntity = new IdEntity();
            idEntity.setId(savedReservation.getId());
            return idEntity;
        } else {
            //Throw error if the Inventory ID does not exist
            throw new InvalidRequestException(ErrorMessages.INVALID_EVENT_IN_RESERVATION);
        }
    }

    /**
     * Deletes a user specified Reservation object from the database
     *
     * @param id
     * @return
     */
    @Override
    public SuccessEntity deleteReservation(Integer id) {
        validateReservationExistence(id);
        reservationRepository.deleteById(id);
        SuccessEntity successEntity = new SuccessEntity();
        successEntity.setSuccess(!reservationRepository.existsById(id));
        return successEntity;
    }

    /**
     * Checks to existene of a Event object in the database
     * @param id
     * @return
     */
    @Override
    public boolean validateEventExistenceById(Integer id) {
        if (!eventRepository.existsById(id)) {
            throw new InvalidRequestException(ErrorMessages.INVALID_ID_EXISTENCE);
        } else {
            return true;
        }
    }

    /**
     * Checks the chronological order of user specified dates
     *
     * @param date1
     * @param date2
     * @return
     */
    @Override
    public boolean dateIsBefore(String date1, String date2) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return simpleDateFormat.parse(date1).before(simpleDateFormat.parse(date2));
        } catch (ParseException e) {
            throw new InvalidRequestException(ErrorMessages.PARSE_ERROR);
        }
    }

    /**
     * Checks to see if a user specified Reservation overlaps with a pre-existing Reservation in the database
     * Упрощенная версия - так как checkIn/checkOut больше не используются, просто проверяем количество
     *
     * @param reservations
     * @return
     */
    @Override
    public boolean reservationOverlaps(Reservation reservations) {
        // Упрощенная логика - так как даты больше не используются,
        // просто возвращаем false (перекрытий нет, так как нет дат для сравнения)
        return false;
    }

    /**
     * Checks the existence of a user specified Reservation object in the database
     *
     * @param id
     * @return
     */
    @Override
    public boolean validateReservationExistence(Integer id) {
        if(!reservationRepository.existsById(id)){
            throw new InvalidRequestException(ErrorMessages.INVALID_ID_EXISTENCE);
        } else {
            return true;
        }
    }
}
