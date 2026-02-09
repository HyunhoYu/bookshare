package my.common.exception;

public class BookCaseNotFoundException extends ApplicationException {
    public BookCaseNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
