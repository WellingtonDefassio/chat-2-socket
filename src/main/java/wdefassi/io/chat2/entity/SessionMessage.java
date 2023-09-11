package wdefassi.io.chat2.entity;

import lombok.Data;
import org.springframework.web.socket.TextMessage;

import java.io.Serializable;

@Data
public class SessionMessage implements Serializable {
    private String sessionFatherId;
    private String message;

    public SessionMessage(String sessionFatherId, String message) {
        this.sessionFatherId = sessionFatherId;
        this.message = message;
    }
}
