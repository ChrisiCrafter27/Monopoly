package monopol.client;

public enum TradeState {
    NULL,
    CHOOSE_PLAYER,
    WAIT_FOR_ACCEPT,
    ACCEPT,
    DENY,
    ABORT,
    CHANGE_OFFER,
    WAIT_FOR_OFFER,
    CONFIRM,
    WAIT_FOR_CONFIRM,
    IN_PROGRESS
}
