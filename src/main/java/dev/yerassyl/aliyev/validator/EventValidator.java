package dev.yerassyl.aliyev.validator;

import dev.yerassyl.aliyev.constants.ErrorMessages;
import dev.yerassyl.aliyev.entity.Event;
import dev.yerassyl.aliyev.entity.ValidTypesOfHotelsEnum;
import dev.yerassyl.aliyev.exception.InvalidRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Slf4j
public class EventValidator extends BaseValidator {

    /**
     * Validator for the Event POST call
     *
     * @param event
     */
    public static void validateEventPOST(Event event) {
        validateName(event.getName());
        validateType(event.getType());
        validateDates(event.getAvailableFrom(), event.getAvailableTo());
    }

    /**
     * Validator for the Event PATCH call
     *
     * @param event
     */
    public static void validateEventPATCH(Event event) {
        validateId(event.getId());
        validateName(event.getName());
        validateType(event.getType());
        validateDates(event.getAvailableFrom(), event.getAvailableTo());
    }

    /**
     * Validator for the Event name
     *
     * @param name
     */
    public static void validateName(String name) {
        if (!StringUtils.hasText(name)) {
            log.error("Event name cannot be null...");
            throw new InvalidRequestException(ErrorMessages.INVALID_NAME);
        }
    }

    /**
     * Validator for the Event type
     *
     * @param type
     */
    public static void validateType(ValidTypesOfHotelsEnum type) {
        if (type == null || !Arrays.asList("CONCERT", "WORKSHOP", "CONFERENCE").contains(type.toString())) {
            log.error("The type parameter: '{}' is invalid, must be one of the following [CONCERT, WORKSHOP, CONFERENCE]", type);
            throw new InvalidRequestException(ErrorMessages.INVALID_TYPE);
        }
    }

}
