package dev.yerassyl.aliyev.validator;

import dev.yerassyl.aliyev.entity.Event;
import dev.yerassyl.aliyev.entity.ValidTypesOfHotelsEnum;
import dev.yerassyl.aliyev.exception.InvalidRequestException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testing the EventValidator class...
 */
class EventValidatorTest {

    @Test
    void validateHotelPOST_ValidCase() {
        Event event = Event.builder()
                .name("Hilton Event")
                .description("5* Event...")
                .availableFrom("2023-01-01")
                .availableTo("2023-12-31")
                .type(ValidTypesOfHotelsEnum.CONCERT)
                .status(true)
                .build();
        Assertions.assertDoesNotThrow(() -> EventValidator.validateEventPOST(event));
    }

    @Test
    void validateHotelPOST_InvalidCase() {
        Event event = Event.builder()
                .name(" ")
                .description("5* Event...")
                .availableFrom("2023-01-01")
                .availableTo("2023-12-31")
                .type(ValidTypesOfHotelsEnum.CONCERT)
                .status(true)
                .build();
        Assertions.assertThrows(InvalidRequestException.class, () -> EventValidator.validateEventPOST(event));
    }

    @Test
    void validateHotelPATCH_ValidCase() {
        Event event = Event.builder()
                .name("Hilton Event")
                .description("5* Event...")
                .availableFrom("2023-01-01")
                .availableTo("2023-12-31")
                .type(ValidTypesOfHotelsEnum.CONCERT)
                .status(true)
                .build();
        Assertions.assertDoesNotThrow(() -> EventValidator.validateEventPOST(event));
    }

    @Test
    void validateId_ValidCase() {
        Integer id = 1;
        Assertions.assertDoesNotThrow(() -> EventValidator.validateId(id));
    }

    @Test
    void validateId_InvalidCase1() {
        Integer id = null;
        Assertions.assertThrows(NullPointerException.class, () -> EventValidator.validateId(id));
    }

    @Test
    void validateName_ValidCase() {
        String name = "Hilton Event";
        Assertions.assertDoesNotThrow(() -> EventValidator.validateName(name));
    }

    @DisplayName("Invalid Event name testing...")
    @Test
    void validateName_InvalidCase() {
        String name = null;
        Assertions.assertThrows(InvalidRequestException.class, () -> EventValidator.validateName(name));
    }

    @Test
    void validateDates_ValidCase1() {
        String from = "2023-01-01";
        String to = "2024-01-01";
        Assertions.assertDoesNotThrow(() -> EventValidator.validateDates(from, to));
    }

    @Test
    void validateDates_InvalidCase1() {
        String from = "2023-01-01";
        String to = "2022-01-01";
        Assertions.assertThrows(InvalidRequestException.class, () -> EventValidator.validateDates(from, to));
    }

    @Test
    void validateDates_InvalidCase2() {
        String from = "Hello";
        String to = "2022-01-01";
        Assertions.assertThrows(InvalidRequestException.class, () -> EventValidator.validateDates(from, to));
    }

    @Test
    void validateDateFormat_ValidCase() {
        String validDate = "2023-01-01";
        Assertions.assertDoesNotThrow(() -> EventValidator.validateDateFormat(validDate));
    }

    @Test
    void validateDateFormat_InvalidCase() {
        String invalidDate = "2023/01/01";
        Assertions.assertThrows(InvalidRequestException.class, () -> EventValidator.validateDateFormat(invalidDate));
    }

}