package dev.yerassyl.aliyev.controller;

import dev.yerassyl.aliyev.dto.IdEntity;
import dev.yerassyl.aliyev.dto.SuccessEntity;
import dev.yerassyl.aliyev.entity.Reservation;
import dev.yerassyl.aliyev.service.ReservationService;
import dev.yerassyl.aliyev.validator.ReservationValidator;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Reservation Controller containing endpoints of Reservation related API calls
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class ReservationController {
    private final ReservationService reservationService;

    /**
     * End point to get all reservations.
     *
     * @return list of Reservations
     */
    @GetMapping(value = "/reservations", produces = "application/json")
    public List<Reservation> getReservationList(){
        log.info("Get all reservations...");
        return reservationService.getAllReservations();
    }
    //jUnit, integrationtest, mockito, hibernate, lombok, jpa, swagger
    /**
     * End point to get user specified reservation.
     *
     * @param id Integer
     * @return Reservation object
     */
    @GetMapping(value = "/reservation/{id}", produces = "application/json")
    public Reservation getReservation(@PathVariable Integer id){
        ReservationValidator.validateId(id);
        log.info("Get a user specified reservation with id = {}", id);
        return reservationService.getReservation(id);
    }

    /**
     * End point to update user specified Reservation
     *
     * @param reservation
     * @param session
     * @return
     */
    @PostMapping(value = "/reservation", produces = "application/json")
    public IdEntity saveReservation(@RequestBody Reservation reservation, HttpSession session){
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            throw new dev.yerassyl.aliyev.exception.InvalidRequestException("Необходимо войти в систему для создания бронирования");
        }
        ReservationValidator.validateReservationPOST(reservation);
        log.info("Save a user specified reservation for userId: {}", userId);
        return reservationService.saveReservation(reservation, userId);
    }

    /**
     * End point to delete user specified Reservation.
     *
     * @param id Integer
     * @return successEntity
     */
    @DeleteMapping(value = "/reservation/{id}", produces = "application/json")
    public SuccessEntity deleteReservation(@PathVariable Integer id){
        ReservationValidator.validateId(id);
        log.info("Delete a user specified reservation...");
        return reservationService.deleteReservation(id);
    }
}
