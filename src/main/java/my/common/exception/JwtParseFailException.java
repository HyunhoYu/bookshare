package my.common.exception;

public class JwtParseFailException extends ApplicationException {

    public JwtParseFailException(ErrorCode errorCode) {
        super(errorCode);
    }
}
