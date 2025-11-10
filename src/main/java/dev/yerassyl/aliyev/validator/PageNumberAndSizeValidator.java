package dev.yerassyl.aliyev.validator;

import dev.yerassyl.aliyev.constants.AppConstants;
import dev.yerassyl.aliyev.exception.InvalidRequestException;

import static dev.yerassyl.aliyev.constants.ErrorMessages.*;

public class PageNumberAndSizeValidator {

    public static void validatePageNumberAndSize(int page, int size) {
        if (page < 0) {
            throw new InvalidRequestException(PAGE_NUMBER_CANNOT_BE_LESS_THAN_ZERO);
        }

        if (size < 0) {
            throw new InvalidRequestException(SIZE_NUMBER_CANNOT_BE_LESS_THAN_ZERO);
        }

        if (size > Integer.parseInt(AppConstants.MAX_PAGE_SIZE)) {
            throw new InvalidRequestException(MAX_PAGE_SIZE_EXCEPTION);
        }
    }
}
