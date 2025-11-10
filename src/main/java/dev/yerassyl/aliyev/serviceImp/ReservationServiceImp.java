package dev.yerassyl.aliyev.serviceImp;

import dev.yerassyl.aliyev.constants.ErrorMessages;
import dev.yerassyl.aliyev.dto.IdEntity;
import dev.yerassyl.aliyev.dto.SuccessEntity;
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
            //boolean to determine if there is already a pre-existing reservation that overlaps with the users
            if (reservationOverlaps(reservations)) {
                throw new InvalidRequestException(ErrorMessages.INVALID_DATE_OVERLAP);
            }
            //determine if the dates are out of the Inventory's bounds  //TODO: getById is deprecated
            if (dateIsBefore(eventRepository.getById(reservations.getEventId()).getAvailableFrom(), reservations.getCheckIn()) && dateIsBefore(reservations.getCheckOut(), eventRepository.getById(reservations.getEventId()).getAvailableTo())) {
                reservations = reservationRepository.save(reservations);
                IdEntity idEntity = new IdEntity();
                idEntity.setId(reservations.getId());
                return idEntity;
            } else {
                throw new InvalidRequestException(ErrorMessages.INVALID_RESERVATION_DATES);
            }
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
        } else if (eventRepository.getById(id).getAvailableFrom() == null && eventRepository.getById(id).getAvailableTo() == null) {  //TODO getById is deprecated
            //Checks if the inventory has available to and available from dates, if not then throw an error as a reservation cannot be made.
            throw new InvalidRequestException(ErrorMessages.EMPTY_EVENT_DATES);
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
     *
     * @param reservations
     * @return
     */
    @Override
    public boolean reservationOverlaps(Reservation reservations) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        return reservationRepository.findAll().stream().anyMatch(dataBaseRes -> {
            //If two reservations have the same inventory id, then compare their check in and checkout dates
            if (dataBaseRes.getEventId() == reservations.getEventId()) {
                try {
                    int checkInBeforeDbCheckOut = sdf.parse(reservations.getCheckIn()).compareTo(sdf.parse(dataBaseRes.getCheckOut()));
                    int checkOutBeforeDbCheckIn = sdf.parse(reservations.getCheckOut()).compareTo(sdf.parse(dataBaseRes.getCheckIn()));
                    log.debug("check in int " + checkInBeforeDbCheckOut);
                    log.debug("check out int " + checkOutBeforeDbCheckIn);
                    if (checkInBeforeDbCheckOut == 0 || checkOutBeforeDbCheckIn == 0) {
                        return true;
                    } else {
                        return checkInBeforeDbCheckOut != checkOutBeforeDbCheckIn;
                    }
                } catch (ParseException e) {
                    throw new InvalidRequestException(ErrorMessages.PARSE_ERROR);
                }
            } else {
                return false;
            }

        });
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
