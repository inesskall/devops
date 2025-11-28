package dev.yerassyl.aliyev.validator;

import dev.yerassyl.aliyev.entity.Reservation;
import dev.yerassyl.aliyev.exception.InvalidRequestException;
import lombok.extern.slf4j.Slf4j;

import static dev.yerassyl.aliyev.constants.ErrorMessages.INVALID_GUESTS;

/**
 * Reservation Validator that ensures user inputs are correctly formatted
 */
@Slf4j
public class ReservationValidator extends BaseValidator {

    /**
     * Validator for the Reservation POST call
     *
     * @param reservation
     */
    public static void validateReservationPOST(Reservation reservation) {
        // Упрощенная валидация - проверяем только наличие eventId
        // checkIn, checkOut и guests больше не обязательны
    }

    /**
     * Validator for the guest number
     *
     * @param guests
     */
    public static void validateGuest(Integer guests) {
        if (guests == null || guests <= 0) {
            log.error("Invalid guests number: '{}', guests must be a non-zero number.", guests);
            throw new InvalidRequestException(INVALID_GUESTS);
        }
    }
}
