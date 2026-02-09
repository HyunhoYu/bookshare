package my.common.exception;

public class BookCaseAlreadyOccupiedException extends ApplicationException {
    public BookCaseAlreadyOccupiedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
