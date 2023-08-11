package monopol.server;

import com.fasterxml.jackson.annotation.JsonFormat;

public enum DisconnectReason {
    SERVER_CLOSED,
    CLIENT_CLOSED,
    CONNECTION_LOST,
    KICKED
}
