package my.common.exception;

public class BookOwnerMismatchException extends ApplicationException {

    public BookOwnerMismatchException(ErrorCode errorCode) {
        super(errorCode);
    }
}
