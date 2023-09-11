package wdefassi.io.chat2.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class StateChat {
    private ArrayList<String> messages;
    private List<WebSocketSession> sessions;

}
