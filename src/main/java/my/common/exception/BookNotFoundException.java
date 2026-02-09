package my.common.exception;

public class BookNotFoundException extends ApplicationException {

    public BookNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
