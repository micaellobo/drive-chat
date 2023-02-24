package Models;

import java.util.Date;

public class Message {

    private final String message;
    private final Date date;
    private final String from;
    private final String to;
    private final TypeMessage type;

    public Message(String message, Date date, String from, String to, TypeMessage type) {
        this.message = message;
        this.date = date;
        this.from = from;
        this.to = to;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public TypeMessage getType() {
        return type;
    }

}
