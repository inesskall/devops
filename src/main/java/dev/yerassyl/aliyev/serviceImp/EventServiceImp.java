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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        if (reservationRepository.findAll().stream()
                .anyMatch(reservations -> reservations.getEventId().equals(id))) {
            throw new InvalidRequestException(ErrorMessages.INVALID_EVENT_DELETE);
        }
        SuccessEntity successEntity = new SuccessEntity();
        eventRepository.deleteById(id);
        successEntity.setSuccess(!eventRepository.existsById(id));
        return successEntity;
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String availTo = event.getAvailableTo();
        String availFrom = event.getAvailableFrom();
        Integer eventId = event.getId();
        List<Reservation> matchingReservationList = reservationRepository.findAll().stream().filter(reservations -> {
            if (reservations.getEventId() == eventId) {
                try {
                    //Checks to see if the user dates are null, if so throw an error as it conflicts with a reservation
                    if (!StringUtils.hasText(availTo) && !StringUtils.hasText(availFrom)) {
                        throw new InvalidRequestException(ErrorMessages.INVALID_DATE_CHANGE_NULL);
                    }
                    //should return 1 or 0 if there is no overlap, should return -1 if there is an overlap
                    int checkInBeforeAvailFrom = sdf.parse(reservations.getCheckIn()).compareTo(sdf.parse(availFrom));
                    //should return -1 or 0 if there is no overlap, should return 1 if there is an overlap
                    int checkOutBeforeAvailTo = sdf.parse(reservations.getCheckOut()).compareTo(sdf.parse(availTo));
                    if ((checkInBeforeAvailFrom < 0) || (checkOutBeforeAvailTo > 0)) {
                        return true;
                    }

                } catch (ParseException e) {
                    throw new InvalidRequestException(ErrorMessages.PARSE_ERROR);
                }
            }
            return false;
        }).toList();

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
