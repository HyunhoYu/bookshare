package my.common.util;

import my.common.exception.ApplicationException;
import my.common.exception.ErrorCode;

public class EntityUtil {

    private EntityUtil() {}

    public static <T> T requireNonNull(T entity, ErrorCode errorCode) {
        if (entity == null) {
            throw new ApplicationException(errorCode);
        }
        return entity;
    }
}
