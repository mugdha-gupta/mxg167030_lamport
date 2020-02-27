package Message;

import java.io.Serializable;

/*
 * Success Message
 * message sent from server to client when message was appended successfully
 */
public class SuccessMessage implements Serializable {
    String successMessge;

    public SuccessMessage(String successMessge) {
        this.successMessge = successMessge;
    }

    public String getSuccessMessge() {
        return successMessge;
    }
}
