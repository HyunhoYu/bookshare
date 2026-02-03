package my.common.exception;

public class InCorrectPasswordException extends ApplicationException {

    public InCorrectPasswordException(ErrorCode errorCode) {
        super(errorCode);
    }
}
