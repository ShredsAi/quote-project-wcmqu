package ai.shreds.application.events;

import org.springframework.context.ApplicationEvent;

public class ApplicationNotificationRequestEvent extends ApplicationEvent {
    private final String type;
    private final String recipientId;
    private final String message;

    public ApplicationNotificationRequestEvent(Object source, String type, String recipientId, String message) {
        super(source);
        this.type = type;
        this.recipientId = recipientId;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public String getMessage() {
        return message;
    }
}
