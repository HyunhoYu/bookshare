package my.common.exception;

public class BookCaseTypeNotFoundException extends ApplicationException {
    public BookCaseTypeNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
