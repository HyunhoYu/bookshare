package my.common.exception;

public class SettlementInsertFailException extends ApplicationException {
    public SettlementInsertFailException(ErrorCode errorCode) {
        super(errorCode);
    }
}
