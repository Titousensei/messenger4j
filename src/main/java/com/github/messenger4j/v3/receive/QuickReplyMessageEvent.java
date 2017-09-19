package com.github.messenger4j.v3.receive;

import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

/**
 * @author Max Grabenhorst
 * @since 1.0.0
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class QuickReplyMessageEvent extends MessageEvent {

    private final String text;
    private final String payload;

    public QuickReplyMessageEvent(@NonNull String senderId, @NonNull String recipientId, @NonNull Instant timestamp,
                                  @NonNull String messageId, @NonNull String text, @NonNull String payload) {
        super(senderId, recipientId, timestamp, messageId);
        this.text = text;
        this.payload = payload;
    }

    public String text() {
        return text;
    }

    public String payload() {
        return payload;
    }
}
