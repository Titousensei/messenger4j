package com.github.messenger4j.v3.receive;

import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

/**
 * @author Max Grabenhorst
 * @since 1.0.0
 */
@ToString
@EqualsAndHashCode
public abstract class BaseEvent {

    private final String senderId;
    private final String recipientId;
    private final Instant timestamp;

    protected BaseEvent(@NonNull String senderId, @NonNull String recipientId, @NonNull Instant timestamp) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.timestamp = timestamp;
    }

    public String senderId() {
        return senderId;
    }

    public String recipientId() {
        return recipientId;
    }

    public Instant timestamp() {
        return timestamp;
    }
}
