package my.common.exception;

public class ForbiddenException extends ApplicationException {
    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode);
    }
}
