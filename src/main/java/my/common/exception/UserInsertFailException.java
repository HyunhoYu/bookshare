package my.common.exception;

public class UserInsertFailException extends ApplicationException {

    public UserInsertFailException(ErrorCode errorCode) {
        super(errorCode);
    }
}
