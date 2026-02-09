package my.common.exception;

public class BookCaseTypeInsertFailException extends ApplicationException {
    public BookCaseTypeInsertFailException(ErrorCode errorCode) {
        super(errorCode);
    }
}
