package Message;

import java.io.Serializable;

public class SuccessMessage implements Serializable {
    String successMessge;

    public SuccessMessage(String successMessge) {
        this.successMessge = successMessge;
    }

    public String getSuccessMessge() {
        return successMessge;
    }
}
