package my.common.exception;

public class UserNotFoundException extends ApplicationException {

    public UserNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
