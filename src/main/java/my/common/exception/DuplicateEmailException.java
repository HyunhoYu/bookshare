package my.common.exception;

public class DuplicateEmailException extends ApplicationException {

    public DuplicateEmailException(ErrorCode errorCode) {
        super(errorCode);
    }
}
