package dev.yerassyl.aliyev.service;

import dev.yerassyl.aliyev.dto.IdEntity;
import dev.yerassyl.aliyev.dto.SuccessEntity;
import dev.yerassyl.aliyev.entity.Reservation;

import java.util.List;

public interface ReservationService {
    List<Reservation> getAllReservations();
    Reservation getReservation(Integer id);
    IdEntity saveReservation(Reservation reservations, Integer userId);
    SuccessEntity deleteReservation(Integer id);
    boolean validateEventExistenceById(Integer id);
    boolean dateIsBefore(String date1, String date2);
    boolean reservationOverlaps(Reservation reservations);
    boolean validateReservationExistence(Integer id);
}
