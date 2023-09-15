package monopol.client;

public enum TradeState {
    NULL,
    CHOOSE_PLAYER,
    WAIT_FOR_ACCEPT,
    ACCEPT,
    DENY,
    ABORT,
    CHANGE_OFFER,
    SEND_OFFER,
    CONFIRM,
    WAIT_FOR_CONFIRM,
    CONFIRMED,
    PERFORM,
    FINISH,
    IN_PROGRESS,
    SERVER_FAIL
}
